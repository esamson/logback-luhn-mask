# Logback Luhn Mask

[![Build Status](https://buildhive.cloudbees.com/job/esamson/job/logback-luhn-mask/badge/icon)](https://buildhive.cloudbees.com/job/esamson/job/logback-luhn-mask/)

This is a [Logback Converter](http://logback.qos.ch/manual/layouts.html#customConversionSpecifier)
that masks any possible credit card numbers in your log messages.

## What is it for?

If you use [Logback](http://logback.qos.ch/), this is an easy way to ensure
that no credit card numbers are exposed through your application logs.

## How do I use it?

First, add the logback-luhn-mask JAR to you runtime classpath. The latest
release is always available from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cph.samson.logback).
If you are using Maven, just add it to your runtime dependencies:

    <dependency>
        <groupId>ph.samson.logback</groupId>
        <artifactId>logback-luhn-mask</artifactId>
        <version>1.0.2</version>
        <scope>runtime</scope>
    </dependency>

Next, in your Logback configuration, define a new *conversionRule* to use the
**LuhnMaskingConverter**. In you appender's pattern configuration, use this new
*conversionRule* where you would usually use `%msg`. For example, if your
existing Logback configuration went like.:

    <configuration>
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%date [%thread] - %msg%n</pattern>
            </encoder>
        </appender>

        <root level="DEBUG">
            <appender-ref ref="STDOUT" />
        </root>
    </configuration>

You would modify it to:

    <configuration>
        <conversionRule conversionWord="maskedMsg" 
                converterClass="ph.samson.logback.luhn.LuhnMaskingConverter" />

        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%date [%thread] - %maskedMsg%n</pattern>
            </encoder>
        </appender>

        <root level="DEBUG">
            <appender-ref ref="STDOUT" />
        </root>
    </configuration>

## How does it work?

The LuhnMaskingConverter takes the [formatted message](http://logback.qos.ch/apidocs/ch/qos/logback/classic/spi/ILoggingEvent.html#getFormattedMessage%28%29)
of the event being logged and scans for consecutive numeric characters that
are long enough to form a credit card number. The space (` `) and dash (`-`)
characters are considered as separators. When such a substring is found, the
[Luhn algorithm](http://en.wikipedia.org/wiki/Luhn_algorithm) is used to check
if it forms a possible credit card number. When a possible credit card number
is found, all its digits except for the last four are replaced with the word
`MASKED` centered in asterisk (`*`) characters. So `5137 0049 8639 6403`
becomes `****MASKED*****6403`.

## How can I help?

Any and all contributions are appreciated.

This project uses maven [Maven](http://maven.apache.org/) and can be built the
usual Maven way.

[Caliper](https://code.google.com/p/caliper/) is used for microbenchmarks. You
can run

    mvn -Pbenchmark

to execute them. Here's a sample run from [my box](https://microbenchmarks.appspot.com/runs/0f18d6d6-452e-4d5a-a4b9-39352ddb86cf).
