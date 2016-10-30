# TinyPass
A tiny password manager.

### How to use
First, install java 8 runtime.

Download and install [Java Cryptography Extension](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html). It is required for AES-256 encryption.

Run the application with command:
```
    java -jar TinyPass.jar [arguments]
```
[arguments] can be:
```
    init                 Initialize a password database
    add arg-name         Add an entry with the specified name
    get [-d] arg-name    Get the entry with the specified name,
                         -d: show description of the entry
    rm arg-name          Remove the entry with the specified name
    find arg-keyword     Search for entries containing the keyword
    gen [arg-length]     Generates a random password with given length
    help                 Show this help message
```

### License
Public domain