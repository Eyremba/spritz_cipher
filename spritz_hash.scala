// A Spritz Cipher driver program to hash files. 

// This implementation is copyright 2015 Richard Todd
//   This program is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 2 of the License, or
//   (at your option) any later version.
// 
//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
// 
//   You should have received a copy of the GNU General Public License
//   along with this program.  If not, see <http://www.gnu.org/licenses/>.

package rwt.spritz

import com.waywardcode.crypto.SpritzCipher
import java.io.FileInputStream

object Hash {
 
  private def doOne(fname: String): Unit = {
     val fstream = new FileInputStream(fname)
     val answer = SpritzCipher.hash(256, fstream) 
     fstream.close()

     print(s"$fname: ")
     answer foreach { printf("%02x",_) }
     println("")
  }

  def main(args: Array[String]): Unit = {
     args foreach doOne
  }

}
