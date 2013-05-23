/*
 * Copyright 2013 samson.ph.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ph.samson.logback.luhn;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Marker;

public class MaskingBenchmark extends Benchmark {

    private static final Map<String, String> tests = new HashMap<String, String>() {
        {
            put("a. Empty String", "");
            put("b. String with no numbers", "String with no numbers");
            put("c. String with not enough numbers", "String with 1234 5678 not enough numbers");
            put("d. String with not enough consecutive numbers", "Not 1234 enough 5678 consecutive 9012 numbers 3456");
            put("e. Non credit card number", "Non 4111 1111 1111 1112 credit card number");
            put("f. Possible credit card number", "Possible 4111 1111 1111 1111 credit card number");
            put("g. Lots of short numbers", "American Express"
                    + "3782822463"
                    + "American Express"
                    + "3714496353"
                    + "American Express Corporate"
                    + "3787344936"
                    + "Australian BankCard"
                    + "5610591081"
                    + "Diners Club"
                    + "3056930902"
                    + "Diners Club"
                    + "3852000002"
                    + "Discover"
                    + "6011111111"
                    + "Discover"
                    + "6011000990"
                    + "JCB"
                    + "3530111333"
                    + "JCB"
                    + "3566002020"
                    + "MasterCard"
                    + "5555555555"
                    + "MasterCard"
                    + "5105105105"
                    + "Visa"
                    + "4111111111"
                    + "Visa"
                    + "4012888888"
                    + "Visa"
                    + "4222222222"
                    + "Note : Even though this number has a different character count than the other test numbers, it is the correct and functional number."
                    + "Processor-specific Cards"
                    + "Dankort (PBS)"
                    + "5019717010"
                    + "Switch/Solo (Paymentech)"
                    + "6331101999");
            put("h. Lots of non credit card numbers", "American Express"
                    + "378282246310006"
                    + "American Express"
                    + "371449635398432"
                    + "American Express Corporate"
                    + "378734493671001"
                    + "Australian BankCard"
                    + "5610591081018251"
                    + "Diners Club"
                    + "30569309025905"
                    + "Diners Club"
                    + "38520000023238"
                    + "Discover"
                    + "6011111111111118"
                    + "Discover"
                    + "6011000990139425"
                    + "JCB"
                    + "3530111333300001"
                    + "JCB"
                    + "3566002020360506"
                    + "MasterCard"
                    + "5555555555554445"
                    + "MasterCard"
                    + "5105105105105102"
                    + "Visa"
                    + "4111111111111112"
                    + "Visa"
                    + "4012888888881882"
                    + "Visa"
                    + "4222222222223"
                    + "Note : Even though this number has a different character count than the other test numbers, it is the correct and functional number."
                    + "Processor-specific Cards"
                    + "Dankort (PBS)"
                    + "5019717010103743"
                    + "Switch/Solo (Paymentech)"
                    + "6331101999990017");
            put("i. Lots of credit card numbers", "American Express"
                    + "378282246310005"
                    + "American Express"
                    + "371449635398431"
                    + "American Express Corporate"
                    + "378734493671000"
                    + "Australian BankCard"
                    + "5610591081018250"
                    + "Diners Club"
                    + "30569309025904"
                    + "Diners Club"
                    + "38520000023237"
                    + "Discover"
                    + "6011111111111117"
                    + "Discover"
                    + "6011000990139424"
                    + "JCB"
                    + "3530111333300000"
                    + "JCB"
                    + "3566002020360505"
                    + "MasterCard"
                    + "5555555555554444"
                    + "MasterCard"
                    + "5105105105105100"
                    + "Visa"
                    + "4111111111111111"
                    + "Visa"
                    + "4012888888881881"
                    + "Visa"
                    + "4222222222222"
                    + "Note : Even though this number has a different character count than the other test numbers, it is the correct and functional number."
                    + "Processor-specific Cards"
                    + "Dankort (PBS)"
                    + "5019717010103742"
                    + "Switch/Solo (Paymentech)"
                    + "6331101999990016");
        }
    };

    @Param({"a. Empty String",
        "b. String with no numbers",
        "c. String with not enough numbers",
        "d. String with not enough consecutive numbers",
        "e. Non credit card number",
        "f. Possible credit card number",
        "g. Lots of short numbers",
        "h. Lots of non credit card numbers",
        "i. Lots of credit card numbers",
    })
    String test;

    public void timeConvertMasked(long reps) {
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        String msg = tests.get(test);

        ILoggingEvent event = new DummyEvent(msg);
        for (int i = 0; i < reps; i++) {
            converter.convert(event);
        }
    }

    public static void main(String[] args) {
        CaliperMain.main(MaskingBenchmark.class, args);
    }

    static class DummyEvent implements ILoggingEvent {

        final String msg;

        DummyEvent(String msg) {
            this.msg = msg;
        }

        @Override
        public String getThreadName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Level getLevel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getMessage() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object[] getArgumentArray() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFormattedMessage() {
            return msg;
        }

        @Override
        public String getLoggerName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public LoggerContextVO getLoggerContextVO() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public IThrowableProxy getThrowableProxy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public StackTraceElement[] getCallerData() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasCallerData() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Marker getMarker() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, String> getMDCPropertyMap() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, String> getMdc() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getTimeStamp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void prepareForDeferredProcessing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
