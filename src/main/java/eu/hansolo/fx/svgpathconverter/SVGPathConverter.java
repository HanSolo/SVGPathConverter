/*
 * Copyright (c) 2021 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.svgpathconverter;

import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public enum SVGPathConverter {
    INSTANCE;

    private static final Pattern PATH_SEGMENT_PATTERN = Pattern.compile("[A-Za-z][^A-Za-z]*");
    private static final Matcher PATH_SEGMENT_MATCHER = PATH_SEGMENT_PATTERN.matcher("");
    private static final Pattern C_SPACE_PATTERN      = Pattern.compile("([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)");
    private static final Matcher C_SPACE_MATCHER      = C_SPACE_PATTERN.matcher("");
    private static final Pattern Q_SPACE_PATTERN      = Pattern.compile("([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)");
    private static final Matcher Q_SPACE_MATCHER      = Q_SPACE_PATTERN.matcher("");

    record Point(double x, double y) {
        public Point(String x, String y) {
            this(Double.parseDouble(x), Double.parseDouble(y));
        }
        public Point(String[] xy) {
            this(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
        }
        public Point(Point p) {
            this(p.x, p.y);
        }
        public Point(double[] xy) {
            this(xy[0], xy[1]);
        }

        @Override public String toString() { return x + "," + y; }
    }

    public String format(final String svgPathString) {
        PATH_SEGMENT_MATCHER.reset(svgPathString);
        StringBuilder pathString = new StringBuilder();
        while(PATH_SEGMENT_MATCHER.find()) {
            final String element    = PATH_SEGMENT_MATCHER.group();
            final String identifier = element.substring(0, 1);
            final String values     = element.substring(1).strip().replaceAll("([0-9]+)-([0-9]+)", "$1,-$2").replaceAll("\\s{2,}", " ").replaceAll("\s,", ",").replaceAll(",\s", ",");
            switch(identifier) {
                case "M", "m",
                         "L", "l" -> {
                    final long noOfCommas = values.chars().filter(ch -> ch == ',').count();
                    if (noOfCommas == 1) {
                        pathString.append(identifier).append(values.replaceAll("\s", ","));
                    } else {
                        Matcher m = Pattern.compile("([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)").matcher(values);
                        while(m.find()) {
                            pathString.append(identifier).append(m.group());
                        }
                    }
                }
                case "H", "h",
                         "V", "v" -> pathString.append(identifier).append(values.replaceAll("\s", ","));
                case "C", "c" -> {
                    final long noOfCommas = values.chars().filter(ch -> ch == ',').count();
                    final long noOfSpaces = values.chars().filter(ch -> ch == ' ').count();
                    if (noOfSpaces == 5 && noOfCommas == 0) {
                        pathString.append(identifier).append(values.replaceAll(C_SPACE_PATTERN.pattern(), "$1,$3 $5,$7 $9,$11"));
                    } else if (noOfCommas == 5) {
                        pathString.append(identifier).append(values.replaceAll("([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)", "$1,$3 $5,$7 $9,$11"));
                    } else if (noOfCommas == 2) {
                        pathString.append(identifier).append(values.replaceAll(",", " ").replaceAll(C_SPACE_PATTERN.pattern(), "$1,$3 $5,$7 $9,$11"));
                    } else if (noOfSpaces == 2) {
                        pathString.append(identifier).append(values.trim());
                    } else if (noOfCommas > 3) {
                        C_SPACE_MATCHER.reset(values.replaceAll(",", " "));
                        while(C_SPACE_MATCHER.find()) {
                            pathString.append(identifier).append(C_SPACE_MATCHER.group(1)).append(",").append(C_SPACE_MATCHER.group(3)).append(" ").append(C_SPACE_MATCHER.group(5)).append(",").append(C_SPACE_MATCHER.group(7)).append(" ").append(C_SPACE_MATCHER.group(9)).append(",").append(C_SPACE_MATCHER.group(11));
                        }
                    }
                }
                case "S", "s" -> {
                    final long noOfCommas = values.chars().filter(ch -> ch == ',').count();
                    final long noOfSpaces = values.chars().filter(ch -> ch == ' ').count();
                    if (noOfCommas == 1) {
                        pathString.append(identifier).append(values.replaceAll(",", " ").replaceAll("([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)", "$1,$3 $5,$7"));
                    } else if (noOfSpaces == 3) {
                        pathString.append(identifier).append(values.replaceAll("([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)", "$1,$3 $5,$7"));
                    } else {
                        pathString.append(identifier).append(values);
                    }
                }
                case "Q", "q" -> {
                    final long noOfCommas = values.chars().filter(ch -> ch == ',').count();
                    final long noOfSpaces = values.chars().filter(ch -> ch == ' ').count();
                    if (noOfCommas == 1) {
                        pathString.append(identifier).append(values.replaceAll(",", " ").replaceAll(Q_SPACE_PATTERN.pattern(), "$1,$3 $5,$7"));
                    } else if (noOfSpaces == 3) {
                        pathString.append(identifier).append(values.replaceAll(Q_SPACE_PATTERN.pattern(), "$1,$3 $5,$7"));
                    } else if (noOfCommas == 3) {
                        pathString.append(identifier).append(values.replaceAll("([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)", "$1,$3 $5,$7"));
                    } else if (noOfCommas > 1) {
                        Q_SPACE_MATCHER.reset(values.replaceAll(",", " "));
                        while(Q_SPACE_MATCHER.find()) {
                            pathString.append(identifier).append(Q_SPACE_MATCHER.group(1)).append(",").append(Q_SPACE_MATCHER.group(3)).append(" ").append(Q_SPACE_MATCHER.group(5)).append(",").append(Q_SPACE_MATCHER.group(7));
                        }
                    } else {
                        pathString.append(identifier).append(values);
                    }
                }
                case "T", "t" -> pathString.append(identifier).append(values.strip().replaceAll("\s", ","));
                case "A", "a" -> {
                    final long noOfCommas = values.chars().filter(ch -> ch == ',').count();
                    final long noOfSpaces = values.chars().filter(ch -> ch == ' ').count();
                    if (noOfSpaces == 6) {
                        pathString.append(identifier).append(values.replaceAll("([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)", "$1,$3 $5 $7 $9 $11,$13"));
                    } else if (noOfCommas == 6) {
                        pathString.append(identifier).append(values.replaceAll("([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)(,)([-0-9]*[\\.0-9]+)", "$1,$3 $5 $7 $9 $11,$13"));
                    } else if (noOfCommas == 2 && noOfSpaces == 4) {
                        pathString.append(identifier).append(values);
                    } else if (noOfCommas == 3) {
                        int indexComma2 = values.indexOf(',', values.indexOf(',') + 1);
                        pathString.append(identifier).append(values, 0, indexComma2).append(" ").append(values.substring(indexComma2 + 1));
                    } else if (noOfCommas == 4) {
                        pathString.append(identifier).append(values.replaceAll(",", " ").replaceAll("([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)(\\s)([-0-9]*[\\.0-9]+)", "$1,$3 $5 $7 $9 $11,$13"));
                    }
                }
                case "B", "b" -> pathString.append(identifier).append(values.strip().replaceAll("\s", ","));
                case "Z", "z" -> pathString.append(identifier);
            }
        }
        return pathString.toString();
    }

    public List<PathElement> convert(final String svgPathString) {
        final List<PathElement> elements = new ArrayList<>();
        PATH_SEGMENT_MATCHER.reset(svgPathString);
        Point lastPoint        = new Point(0, 0);
        Point lastControlPoint = new Point(0, 0);
        double bearing         = 0;
        double lastBearing     = 0;
        while(PATH_SEGMENT_MATCHER.find()) {
            final String element    = PATH_SEGMENT_MATCHER.group();
            final String identifier = element.substring(0, 1);
            final String values     = element.substring(1);
            switch(identifier) {
                case "M" -> {
                    final String[] xy = values.split(",");
                    elements.add(new MoveTo(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
                    lastPoint  = new Point(xy);
                }
                case "m" -> {
                    final String[] xy = values.split(",");
                    final Point    p  = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new MoveTo(p.x, p.y));
                    lastPoint  = new Point(p.x, p.y);
                }
                case "L" -> {
                    final String[] xy = values.split(",");
                    elements.add(new LineTo(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
                    lastPoint = new Point(xy);
                }
                case "l" -> {
                    final String[] xy = values.split(",");
                    final Point    p  = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new LineTo(p.x, p.y));
                    lastPoint = p;
                }
                case "H" -> {
                    final Point p = new Point(Double.parseDouble(values), lastPoint.y);
                    elements.add(new LineTo(p.x, p.y));
                    lastPoint = p;
                }
                case "h" -> {
                    final Point p;
                    if (bearing == 0) {
                        p = new Point(lastPoint.x + Double.parseDouble(values), lastPoint.y);
                    } else {
                        p = new Point(rotatePointAroundRotationCenter(lastPoint.x + Double.parseDouble(values), lastPoint.y, lastPoint.x, lastPoint.y, bearing));
                    }
                    elements.add(new LineTo(p.x, p.y));
                    lastPoint = p;
                }
                case "V" -> {
                    final Point p = new Point(lastPoint.x, Double.parseDouble(values));
                    elements.add(new LineTo(p.x, p.y));
                    lastPoint = p;
                }
                case "v" -> {
                    final Point p = new Point(lastPoint.x, lastPoint.y + Double.parseDouble(values));
                    elements.add(new LineTo(p.x, p.y));
                    lastPoint = p;
                }
                case "C" -> {
                    final String[] groups   = values.split(" ");
                    final String[] control1 = groups[0].split(",");
                    final String[] control2 = groups[1].split(",");
                    final String[] xy       = groups[2].split(",");
                    elements.add(new CubicCurveTo(Double.parseDouble(control1[0]), Double.parseDouble(control1[1]),
                                                  Double.parseDouble(control2[0]), Double.parseDouble(control2[1]),
                                                  Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
                    lastPoint        = new Point(xy);
                    lastControlPoint = new Point(control2);
                }
                case "c" -> {
                    final String[] groups   = values.split(" ");
                    final String[] control1 = groups[0].split(",");
                    final String[] control2 = groups[1].split(",");
                    final String[] xy       = groups[2].split(",");
                    final Point    c1       = new Point(lastPoint.x + Double.parseDouble(control1[0]), lastPoint.y + Double.parseDouble(control1[1]));
                    final Point    c2       = new Point(lastPoint.x + Double.parseDouble(control2[0]), lastPoint.y + Double.parseDouble(control2[1]));
                    final Point    p        = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new CubicCurveTo(c1.x, c1.y, c2.x, c2.y, p.x, p.y));
                    lastPoint        = p;
                    lastControlPoint = c2;
                }
                case "S" -> {
                    final String[] groups   = values.split(" ");
                    final String[] control2 = groups[0].split(",");
                    final String[] xy       = groups[1].split(",");
                    final Point   c1        = new Point(rotatePointAroundRotationCenter(lastControlPoint.x, lastControlPoint.y, lastPoint.x, lastPoint.y, 180));
                    elements.add(new CubicCurveTo(c1.x, c1.y, Double.parseDouble(control2[0]), Double.parseDouble(control2[1]), Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
                    lastPoint        = new Point(xy);
                    lastControlPoint = new Point(control2);
                }
                case "s" -> {
                    final String[] groups   = values.split(" ");
                    final String[] control2 = groups[0].split(",");
                    final String[] xy       = groups[1].split(",");
                    final Point    c1       = new Point(rotatePointAroundRotationCenter(lastControlPoint.x, lastControlPoint.y, lastPoint.x, lastPoint.y, 180));
                    final Point    c2       = new Point(lastPoint.x + Double.parseDouble(control2[0]), lastPoint.y + Double.parseDouble(control2[1]));
                    final Point    p        = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new CubicCurveTo(c1.x, c1.y, c2.x, c2.y, p.x, p.y));
                    lastPoint        = p;
                    lastControlPoint = c2;
                }
                case "Q" -> {
                    final String[] groups   = values.split(" ");
                    final String[] control1 = groups[0].split(",");
                    final String[] xy       = groups[1].split(",");
                    elements.add(new QuadCurveTo(Double.parseDouble(control1[0]), Double.parseDouble(control1[1]),
                                                 Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
                    lastPoint        = new Point(xy);
                    lastControlPoint = new Point(control1);
                }
                case "q" -> {
                    final String[] groups   = values.split(" ");
                    final String[] control1 = groups[0].split(",");
                    final String[] xy       = groups[1].split(",");
                    final Point    c1       = new Point(lastPoint.x + Double.parseDouble(control1[0]), lastPoint.y + Double.parseDouble(control1[1]));
                    final Point    p        = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new QuadCurveTo(c1.x, c1.y, p.x, p.y));
                    lastPoint        = p;
                    lastControlPoint = c1;
                }
                case "T" -> {
                    final String[] groups = values.split(" ");
                    final String[] xy     = groups[0].split(",");
                    final Point    c1     = new Point(rotatePointAroundRotationCenter(lastControlPoint.x, lastControlPoint.y, lastPoint.x, lastPoint.y, 180));
                    elements.add(new QuadCurveTo(c1.x, c1.y, Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
                    lastPoint        = new Point(xy);
                    lastControlPoint = c1;
                }
                case "t" -> {
                    final String[] groups = values.split(" ");
                    final String[] xy     = groups[0].split(",");
                    final Point c1        = new Point(rotatePointAroundRotationCenter(lastControlPoint.x, lastControlPoint.y, lastPoint.x, lastPoint.y, 180));
                    final Point p         = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new QuadCurveTo(c1.x, c1.y, p.x, p.y));
                    lastPoint        = p;
                    lastControlPoint = c1;
                }
                case "A" -> {
                    final String[] groups   = values.split(" ");
                    final String[] radius   = groups[0].split(",");
                    final String   rotation = groups[1];
                    final String   largeArc = groups[2];
                    final String   sweep    = groups[3];
                    final String[] xy       = groups[4].split(",");
                    elements.add(new ArcTo(Double.parseDouble(radius[0]), Double.parseDouble(radius[1]), Double.parseDouble(rotation), Double.parseDouble(xy[0]), Double.parseDouble(xy[1]), largeArc.equals("1"), sweep.equals("1")));
                    lastPoint = new Point(xy);
                }
                case "a" -> {
                    final String[] groups   = values.split(" ");
                    final String[] radius   = groups[0].split(",");
                    final String   rotation = groups[1];
                    final String   largeArc = groups[2];
                    final String   sweep    = groups[3];
                    final String[] xy       = groups[4].split(",");
                    final Point    rp       = new Point(Double.parseDouble(radius[0]), Double.parseDouble(radius[1]));
                    final Point    p        = new Point(lastPoint.x + Double.parseDouble(xy[0]), lastPoint.y + Double.parseDouble(xy[1]));
                    elements.add(new ArcTo(rp.x, rp.y, Double.parseDouble(rotation), p.x, p.y, largeArc.equals("1"), sweep.equals("1")));
                    lastPoint = p;
                }
                case "B" -> {
                    bearing     = Double.parseDouble(values);
                    lastBearing = bearing;
                }
                case "b" -> {
                    bearing     = (lastBearing + Double.parseDouble(values)) % 360.0;
                    lastBearing = bearing;
                }
                case "Z", "z" -> {
                    elements.add(new ClosePath());
                }
            }
        }
        return elements;
    }

    public static final double[] rotatePointAroundRotationCenter(final double x, final double y, final double rX, final double rY, final double angleDeg) {
        final double rad = Math.toRadians(angleDeg);
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);
        final double nX  = rX + (x - rX) * cos - (y - rY) * sin;
        final double nY  = rY + (x - rX) * sin + (y - rY) * cos;
        return new double[] { nX, nY };
    }
}
