/*
  The MIT License (MIT)

  Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)

  Permission is hereby granted, free of charge, to any person obtaining a copy of
  this software and associated documentation files (the "Software"), to deal in
  the Software without restriction, including without limitation the rights to
  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.loaders;

import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class builds a Ta4j time series from a CSV file containing trades.
 */
public class CsvTicksLoader {

    static final String SEPARATOR = ";";
    static final String _SEPARATOR = ",";

    public static Map<String, Integer> fields = new HashMap<>();

    public static Map<String, Integer> _fields = new HashMap<>();

    static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy.MM.dd")
            .toFormatter();

    static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy.MM.dd HH:mm:ss")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    static final DateTimeFormatter _DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy.MM.dd H:mm:ss")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    static final DateTimeFormatter __DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy.MM.dd HH:mm")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter();

    static {
        fields = new LinkedHashMap<>();
        fields.put("DATE", 0);
        fields.put("TIME", 1);
        fields.put("OPEN", 2);
        fields.put("HIGH", 3);
        fields.put("LOW", 4);
        fields.put("CLOSE", 5);
        fields.put("VOLUME", 6);
    }

    static {
        _fields = new LinkedHashMap<>();
        _fields.put("DATE", 0);
        _fields.put("OPEN", 1);
        _fields.put("HIGH", 2);
        _fields.put("LOW", 3);
        _fields.put("CLOSE", 4);
        _fields.put("VOLUME", 5);
    }

    public static String assertHeader(List<String> fields, List<String> header) {
        for (String field : fields) {
            if(!header.contains(field)) {
                return field;
            }
        }
        return "";
    }

    /**
     * @return a time series from Apple Inc. ticks.
     */
    public static TimeSeries load(String name) {

        TimeSeries series = new BaseTimeSeries(name);

        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            String separator;
            String line;
            String[] tokens;
            // Se comprueba el encabezado del archivo
            String header = reader.readLine();
            if(header.contains(";") || header.contains(",")) {
                if(header.contains(";")) {
                    separator = SEPARATOR;
                }
                else {
                    separator = _SEPARATOR;
                }
            }
            else {
                throw new IllegalArgumentException("El encabezado no contiene un separador válido. Separadores válidos: ';' y ','");
            }

            List<String> headerFields = Arrays.asList(header.split(separator));
            String field;

            switch (headerFields.size()) {
                // Si tiene 6 campos, entonces son barras con solo fecha
                case 6:
                    field = assertHeader((List<String>) (Object) Arrays.asList(_fields.keySet().toArray()), headerFields);

                    if(!field.isEmpty()) {
                        throw new IllegalArgumentException("El encabezado no contiene el campo '" + field + "'");
                    }

                    while ((line = reader.readLine()) != null) {
                        tokens = line.split(separator);
                        ZonedDateTime time = LocalDate.parse(tokens[0], DATE_FORMATTER).atStartOfDay(ZoneId.systemDefault());

                        double open = Double.parseDouble(tokens[1].replace(",","."));
                        double high = Double.parseDouble(tokens[2].replace(",","."));
                        double low = Double.parseDouble(tokens[3].replace(",","."));
                        double close = Double.parseDouble(tokens[4].replace(",","."));
                        double volume = Double.parseDouble(tokens[5].replace(",","."));

                        series.addBar(new BaseBar(time, open, high, low, close, volume));
                    }

                    break;
                // Si tiene 6 campos, entonces son barras con fecha y hora
                case 7:
                    field = assertHeader((List<String>) (Object) Arrays.asList(_fields.keySet().toArray()), headerFields);

                    if(!field.isEmpty()) {
                        throw new IllegalArgumentException("El encabezado no contiene el campo '" + field + "'");
                    }

                    while ((line = reader.readLine()) != null) {
                        tokens = line.split(separator);
                        LocalDateTime time;

                        if(tokens[1].length() == 7) {
                            time = LocalDateTime.parse(tokens[0] + " " + tokens[1], _DATE_TIME_FORMATTER);
                        }
                        else {
                            try {
                                time = LocalDateTime.parse(tokens[0] + " " + tokens[1], DATE_TIME_FORMATTER);
                            }
                            catch (DateTimeParseException e) {
                                time = LocalDateTime.parse(tokens[0] + " " + tokens[1], __DATE_TIME_FORMATTER);
                            }
                        }

                        double open = Double.parseDouble(tokens[2].replace(",","."));
                        double high = Double.parseDouble(tokens[3].replace(",","."));
                        double low = Double.parseDouble(tokens[4].replace(",","."));
                        double close = Double.parseDouble(tokens[5].replace(",","."));
                        double volume = Double.parseDouble(tokens[6].replace(",","."));

                        series.addBar(new BaseBar(time.atZone(ZoneId.systemDefault()), open, high, low, close, volume));
                    }

                    break;
                default:
                    throw new IllegalArgumentException("El encabezado contiene " + headerFields.size() + "campos. Números válidos de campos 5 y 6");
            }

        } catch (IOException ioe) {
            Logger.getLogger(CsvTicksLoader.class.getName()).log(Level.SEVERE, "Unable to load ticks from CSV", ioe);
        } catch (NumberFormatException nfe) {
            Logger.getLogger(CsvTicksLoader.class.getName()).log(Level.SEVERE, "Error while parsing value", nfe);
        }

        return series;
    }

}
