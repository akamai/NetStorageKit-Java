# NetStorageKit (for Java)

## Important
Akamai does not maintain or regulate this package. While it can be incorporated to assist you in API use, Akamai Technical Support will not offer assistance and Akamai cannot be held liable if issues arise from its use. 

## Overview
This library assists in the interaction with Akamai's NetStorage CMS API. The following CMS API Specs are available:

* [FileStore-format](https://control.akamai.com/dl/customers/NS/NS_http_api_FS.pdf)
* [ObjectStore-format](https://control.akamai.com/dl/customers/NS/NS_http_api_OS.pdf)

## Project organization
* /src - project sources
* /test - junit test cases
* /build - build and output libraries
* /doc - javadoc for class libraries
* /lib - 3rd party dependency libraries (Currenly only necessary for running unit tests)
* /example - an example app that utilizes the NetStorageKit

## Install
* Compile the sources from `/src` into `build/classes`.
* An ant `build.xml` is also provided for ease of use. Common commands are: `ant compile`, `ant jar` and `ant test`
* A maven `pom.xml` is also provided for ease of use. To install: `mvn install`
* Both IntelliJ and Eclipse project files are available
* the destination jar file is located in build/jar

## Getting Started
* Create an instance of the `NetStorage` object by passing in the host, username and key
* Issue a command to NetStorage by calling the appropriate method from the `NetStorage` object

For example, to delete a file:
```
import com.akamai.netstorage.NetStorage;
import com.akamai.netstorage.DefaultCredential;

DefaultCredential credential = new DefaultCredential("example.akamaihd.net","id of your upload account", "apiKey of upload account");

NetStorage ns = new NetStorage(credential);
ns.delete("/[CP Code]/example.zip");
```

Other methods return an `InputStream`. For example, to retrieve a directory listing:

```
import com.akamai.netstorage.NetStorage;
import com.akamai.netstorage.DefaultCredential;
DefaultCredential credential = new DefaultCredential("example.akamaihd.net","id of your upload account", "apiKey of upload account");
NetStorage ns = new NetStorage(credential);

try (InputStream result = ns.dir("/[CP code]/1234")) {
 // TODO: consume InputStream
}
```

Finally, when uploading a `File` object can be sent or an open `InputStream` wll be used
```
import com.akamai.netstorage.NetStorage;
import com.akamai.netstorage.DefaultCredential;
DefaultCredential credential = new DefaultCredential("example.akamaihd.net","id of your upload account", "apiKey of upload account");
NetStorage ns = new NetStorage(credential);
try (InputStream result = ns.upload("/[CP code]/1234/example.zip", new File("../workingdir/srcfile.zip"))) {
 // TODO: consume InputStream
}
```


## Sample application (CMS)
* A sample application has been created that can take command line parameters.

```
java -classpath build/classes CMS -a dir -u user1 -k 1234abcd example.akamaihd.net/1234
```
