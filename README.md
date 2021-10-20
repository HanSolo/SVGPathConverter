## SVGPathConverter

The SVGPathConverter is a little tool that helps you converting SVG paths to JavaFX paths.

It has two methods, one to format a SVG path string and the other to convert it to a list of JavaFX PathElements.

e.g.
```java
String            svgString = "M 54.1,12.5 12.9,54.7 C -2.7,70.3 23,69 32.3,74.9 36.6,77.7 18.5,81.3 22.2,85 c 3.6,3.7 21.7,7.1 25.3,10.7 3.6,3.7 -7.3,7.6 -3.7,11.3 3.5,3.7 11.9,0.2 13.4,8.6 1.1,6.2 15.4,3.1 21.8,-2.2 4,-3.4 -6.9,-3.4 -3.3,-7.1 9,-9.1 17,-4.1 20.3,-12.5 1.8,-4.5 -13.6,-7.7 -9.5,-10.6 9.8,-6.9 45.8,-10.4 29.2,-27 L 73,12.5 c -5.3,-5 -14,-5 -18.9,0 z m -9.9,64.7 c 0.9,0 30.8,4 19.3,7.1 -4.4,1.2 -24.6,-7.1 -19.3,-7.1 z m 57.2,16.6 c 0,2.1 16.3,3.3 15.4,-0.5 -1.3,-6.4 -13.6,-5.9 -15.4,0.5 z m -69.5,11.1 c 3.7,3.2 9.3,-0.7 11.1,-5.2 -3.6,-4.7 -16.9,0.3 -11.1,5.2 z m 67.5,-6.7 c -4.6,4.2 0.8,8.6 5.3,5.7 1.2,-0.8 -0.1,-4.7 -5.3,-5.7 z";
List<PathElement> elements  = SVGPathConverter.INSTANCE.convert(SVGPathConverter.INSTANCE.format(svgString));
Path              path      = new Path(elements);
```

The path can then be used in a JavaFX scenegraph.