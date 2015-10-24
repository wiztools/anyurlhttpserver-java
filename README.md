## Any Url Http Server

A HTTP server that responds with the content of the input file, irrespective of what path is requested. Can be used for quickly mocking a API response for dev / testing purposes.

### Build

```
$ gradle build
```

### Run

```
$ java -jar build/libs/anyurlhttpserver.jar -p 3000 -f a.html
```

When the above command is executed, any URL that is hit at [http://localhost:3000/](http://localhost:3000/) will serve the contents of `a.html`.

To see help:

```
$ java -jar build/libs/anyurlhttpserver.jar -h
```

[![](https://farm1.staticflickr.com/567/22414841026_ee9ba931c1_o.png)](https://www.flickr.com/photos/subwiz/22414841026/)
