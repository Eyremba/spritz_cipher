all: libspritz.a spritz-hash spritz-crypt

# Change this to affect where stuff gets installed...
PREFIX=${HOME}

# if your commands are somewhere wierd or not on the path,
# then set these variables...
CC=cc
AR=ar
STRIP=strip
MKDIR=mkdir
MV=mv
CP=cp
RM=rm

# Change this to adjust build options
CCOPT=-std=c11 -march=native -O3

clean:
	-${RM} *.o *.a *~ spritz-hash spritz-crypt

test: all
	test/test.sh

install: all
	${MKDIR} -p ${PREFIX}/bin
	${MV} spritz-hash spritz-crypt ${PREFIX}/bin
	${MKDIR} -p ${PREFIX}/lib
	${MV} libspritz.a ${PREFIX}/lib
	${MKDIR} -p ${PREFIX}/include
	${CP} spritz.h ${PREFIX}/include
	${MKDIR} -p ${PREFIX}/man/man1
	${CP} spritz-crypt.1 spritz-hash.1 ${PREFIX}/man/man1

libspritz.a: spritz.h spritz.c
	${CC} ${CCOPT} -c spritz.c 
	${AR} -r libspritz.a spritz.o

spritz-crypt: spritz.h crypt.c  libspritz.a
	${CC} ${CCOPT} crypt.c  -L. -lspritz -o spritz-crypt
	${STRIP} spritz-crypt

spritz-hash: spritz.h hash.c b64.c libspritz.a
	${CC} ${CCOPT} hash.c b64.c -L. -lspritz -o spritz-hash
	${STRIP} spritz-hash
