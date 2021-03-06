# C Version

## (2016-02-27) Concurrency Via xargs

I decided my previous two versions (creating a server/client model
via fork/exec) were more complicated than the situation warranted.
So, while they worked well and will live forever in this repo's history,
I decided to simplify them.

I've made spritz-crypt and spritz-hash both single-threaded, single-process
commands, and am content to launch multiple copies of them
via `xargs`.  This example runs 4 hashes concurrently, running each process
it spawns on five files:

    find . -type f | xargs -n 5 -P 4 spritz-hash

It won't be as efficient at load-balancing as the client-server is, 
but for my use-cases I won't ever care.  This is a better balance
between features and complexity for my purposes.

## (2016-02-21) Concurrency Via Perl Script

In keeping with Unix philosophy, I separated concerns into a 
perl front-end and a C back-end. Compared to the pure C fork()-ing
version I had before, the code is much simpler and performance 
is the same.

With this design, a user of the code has several options:

  * Call/modify the perl script
  * Run the C binary, speak its simple textual language
  * Link to the `libspritz` library, call on the functionality

I believe this design offers the best of all worlds:

  * Easy to change surface details of the UI via perl/python/whatever
  * Fast processing spread across a cores in a process pool
  * More straightforward C code, complexity pushed out to a script 

One downside to this approach: now there's a dependency on perl if you want 
to run the default font-end. However, the previous all-C version used
POSIX calls, and if you have POSIX you probably have access to perl. 

As before, the parent (perl) sends simple commands to the worker (C) process. For
example, a command to encrypt looks like:

    "E file.txt file.txt.spritz"

... and if the encryption goes well, the the worker responds:

    "OK file.txt -encrypt-> file.txt.spritz"

It's easy to test the C code by hand on the terminal, which is a plus.


## (2016-02-14) Concurrency Via Fork()

I wanted to see how a concurrent model based on fork would compare
with my [go version in the other repo](https://github.com/waywardcode/spritz_go).
So, tonight I added a `-j` option to the c hasher like _make_ has for 
how many concurrent jobs to spin up.  I defined a simple
language for the worker processes to communicate back up the parent (via pipes):

    "OK <msg>" ==> print the msg to stdout and give me more work 
    "ER <msg>" ==> print this error message, and give me more work

The results, hashing 1.6GB of files took:

    time:   50s for the c version with 8 jobs (-j8)
    time: 1m23s for the Go version (max procs = 8)

So, the c version is a good bit faster, but I had to work _a lot_ harder at the
concurrency.  I had to:

  * manually fork off the processes, create the pipes, and hook them up.
  * devise a protocol for them to speak to each other
  * implement the protocol, including all the necessary error-handling
    and buffering

Meanwhile, in Go, converting the serial version to the concurrent version just took a couple
lines of code. So, on any given project, you have to decide how much performance you are 
willing to trade for convenience. 

## Original Implementation Notes

I wanted to see how much faster a C version of the hasher would
be, compared to the java version.  Imagine my surprise that the
java version is slightly faster!

I tried turning off the buffering on the `FILE *` in case
that was the problem, but that only slightly changed the
outcome.

My initial try, surprisingly, was slower than the java version.
On a 460MB file, it ran in 79 seconds vs. java's 60 seconds.

However, looking at compiler's assembly output for the innermost loop,
I realized I could make an important optimization.  Here's the 
unoptimized loop:

```
static void update(spritz_state s, int times) {
  while(times--) {
    s->i += s->w;
    s->j = s->k + smem(s->j+s->mem[s->i]);
    s->k = s->i + s->k + s->mem[s->j];
    swap(s->mem, s->i, s->j);
  }
}
```

... and here's the optimized version:

```
static void update(spritz_state s, int times) {
  uint8_t mi = s->i;
  uint8_t mj = s->j;
  uint8_t mk = s->k;
  const uint8_t mw = s->w ;
  
  while(times--) {
    mi += mw;
    mj = mk + smem(mj+s->mem[mi]);
    mk = mi + mk + s->mem[mj];
    swap(s->mem, mi, mj);
  }
 
  s->i = mi;
  s->j = mj;
  s->k = mk;
}
```

In the optimized version, I'm helping the compiler to see that it
doesn't need to store the intermediate values of `i`, `j`, and `k`
back into the state structure until it's done with all the iterations.

With that change, the C version takes 54 seconds against java's 60. Still
not a big win for C... and I chalk that up to my naive use of `fread`
vs. java's probably much more optimized I/O.  


