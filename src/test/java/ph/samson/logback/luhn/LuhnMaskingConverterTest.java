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

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static ph.samson.logback.luhn.LuhnMaskingConverter.*;

public class LuhnMaskingConverterTest {

    @Test
    public void testHasEnoughDigitsFormattedMessageNull() {
        assertEquals(false, hasEnoughDigits(null));
    }

    @Test
    public void testMaskFormattedMessageNull() {
        assertEquals(null, mask(null));
    }

    @Test
    public void testStripSeparatorsSpaces() {
        assertEquals("5137004986396403", stripSeparators("5137 0049 8639 6403"));
    }

    @Test
    public void testStripSeparatorsDashes() {
        assertEquals("5137004986396403", stripSeparators("5137-0049-8639-6403"));
    }

    @Test
    public void testMaskString() {
        assertEquals("***MASKED***", maskString("5137004986396403", "6403"));
    }

    @Test
    public void testLuhnGood() {
        assertTrue(luhnCheck("5137004986396403"));
    }

    @Test
    public void testLuhnBad() {
        assertFalse(luhnCheck("5137004986396404"));
    }

    /**
     * Only possible credit card numbers are masked.
     */
    @Test
    public void testConvert() {
        String msg = "try 5137 0049 8639 6404 and 5137 0049 8639 6403";
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent e = mock(ILoggingEvent.class);
        when(e.getFormattedMessage()).thenReturn(msg);
        assertEquals("try 5137 0049 8639 6404 and ****MASKED*****6403", converter.convert(e));
    }

    /**
     * Only possible credit card numbers are masked.
     */
    @Test
    public void testConvertCcNumberAtStartNonCcNumberAtEnd() {
        String msg = "5137 0049 8639 6403 and 5137 0049 8639 6404";
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent e = mock(ILoggingEvent.class);
        when(e.getFormattedMessage()).thenReturn(msg);
        assertEquals("****MASKED*****6403 and 5137 0049 8639 6404", converter.convert(e));
    }

    /**
     * Only possible credit card numbers are masked.
     */
    @Test
    public void testConvertMultiple() {
        String msg = "try 5137 0049 8639 6403 multiple 5137 0049 8639 6404 possible 4111-1111-1111 1111 card 4111111111111112 numbers";
        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent e = mock(ILoggingEvent.class);
        when(e.getFormattedMessage()).thenReturn(msg);
        assertEquals("try ****MASKED*****6403 multiple 5137 0049 8639 6404 possible ****MASKED*****1111 card 4111111111111112 numbers", converter.convert(e));
    }

    @Test
    public void testLotsOfCcNumbers() {
        String msg
                = "American Express"
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
                + "6331101999990016";

        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent e = mock(ILoggingEvent.class);
        when(e.getFormattedMessage()).thenReturn(msg);
        assertEquals(
                "American Express"
                + "**MASKED***0005"
                + "American Express"
                + "**MASKED***8431"
                + "American Express Corporate"
                + "**MASKED***1000"
                + "Australian BankCard"
                + "***MASKED***8250"
                + "Diners Club"
                + "**MASKED**5904"
                + "Diners Club"
                + "**MASKED**3237"
                + "Discover"
                + "***MASKED***1117"
                + "Discover"
                + "***MASKED***9424"
                + "JCB"
                + "***MASKED***0000"
                + "JCB"
                + "***MASKED***0505"
                + "MasterCard"
                + "***MASKED***4444"
                + "MasterCard"
                + "***MASKED***5100"
                + "Visa"
                + "***MASKED***1111"
                + "Visa"
                + "***MASKED***1881"
                + "Visa"
                + "*MASKED**2222"
                + "Note : Even though this number has a different character count than the other test numbers, it is the correct and functional number."
                + "Processor-specific Cards"
                + "Dankort (PBS)"
                + "***MASKED***3742"
                + "Switch/Solo (Paymentech)"
                + "***MASKED***0016",
                converter.convert(e));
    }

    @Test
    public void testLotsOfNonCcNumbers() {
        String msg
                = "American Express"
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
                + "6331101999990017";

        LuhnMaskingConverter converter = new LuhnMaskingConverter();
        ILoggingEvent e = mock(ILoggingEvent.class);
        when(e.getFormattedMessage()).thenReturn(msg);
        assertEquals(
                "American Express"
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
                + "6331101999990017",
                converter.convert(e));
    }
}
