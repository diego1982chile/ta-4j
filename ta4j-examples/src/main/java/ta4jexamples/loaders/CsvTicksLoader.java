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
import java.time.temporal.ChronoField;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class builds a Ta4j time series from a CSV file containing trades.
 */
public class CsvTicksLoader {

    static final String SEPARATOR = ";";

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

    /**
     * @return a time series from Apple Inc. ticks.
     */
    public static TimeSeries load(String name) {

        TimeSeries series = new BaseTimeSeries(name);

        try (BufferedReader reader = new BufferedReader(new FileReader(name))) {
            String line;
            String[] tokens;
            // Se descarta el encabezado del archivo
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                tokens = line.split(SEPARATOR);
                LocalDateTime time;

                if(tokens.length == 8) {
                    time = LocalDate.parse(tokens[0], DATE_FORMATTER).atStartOfDay();
                }
                else {
                    if(tokens[1].length() == 7) {
                        time = LocalDateTime.parse(tokens[0] + " " + tokens[1], _DATE_TIME_FORMATTER);
                    }
                    else {
                        time = LocalDateTime.parse(tokens[0] + " " + tokens[1], DATE_TIME_FORMATTER);
                    }
                }

                double open = Double.parseDouble(tokens[2]);
                double high = Double.parseDouble(tokens[3]);
                double low = Double.parseDouble(tokens[4]);
                double close = Double.parseDouble(tokens[5]);
                double volume = Double.parseDouble(tokens[6]);

                series.addBar(new BaseBar(time.atZone(ZoneId.of("America/Santiago")), open, high, low, close, volume));
            }
        } catch (IOException ioe) {
            Logger.getLogger(CsvTicksLoader.class.getName()).log(Level.SEVERE, "Unable to load ticks from CSV", ioe);
        } catch (NumberFormatException nfe) {
            Logger.getLogger(CsvTicksLoader.class.getName()).log(Level.SEVERE, "Error while parsing value", nfe);
        }

        return series;
    }

}
