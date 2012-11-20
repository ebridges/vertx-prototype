vertx-prototype
===============

Illustration using vert.x as a replacement for socket_test.cgi

### vert.x installation
1. Download and uncompress vert.x [v.1.2.3 FINAL](http://vertx.io/downloads.html)
2. Install in folder `/opt/vert.x-1.2.3.final` and create a symlink named `/opt/vert.x` that points to it.
3. Add `/opt/vert.x/bin` to your path.

### prototype information
#### purpose
* Mimic the behavior of `socket_test.cgi`.
	* _Tested with football_mini_client1.1.4.air_.

#### goals
* Demonstrate simpler development process when using vert.x compared with Perl.
* Demonstrate a coding model very similar to Node.js.
	* _note: it is possible to use a module to deploy JS written for Node to vert.x, however this is not illustrated here._

#### running
* JavaScript:
```javascript
$ vertx run socket_test.js
```

* Java:
```java
$ vertx run SocketTest.java
```
	- _note: no need to compile_

### stats
Lines of Code
* `socket_test.js` :  36 lines
* `socket_test.cgi` : 181 lines
* `SocketTest.java` : 113 lines
	- _note: `SocketTest.java` contains a few other, strictly unnecessary, classes to handle Logging and to better structure the response._
