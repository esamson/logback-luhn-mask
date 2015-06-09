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

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * LuhnMaskingConverter replaces sequences of digits that pass the Luhn check
 * with a masking string, leaving only the suffix containing the last four
 * digits.
 * 
 * To use, define a new conversion word in your Logback configuration. E.g.,
 * 
 *     <configuration>
 *         <conversionRule conversionWord="maskedMsg" 
 *             converterClass="ph.samson.logback.luhn.LuhnMaskingConverter" />
 *         <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
 *             <encoder>
 *                 <pattern>%date [%thread] - %maskedMsg%n</pattern>
 *             </encoder>
 *         </appender>
 *         <root level="DEBUG">
 *             <appender-ref ref="STDOUT" />
 *         </root>
 *     </configuration>
 *
 * @author Edward Samson <edward@samson.ph>
 */
public class LuhnMaskingConverter extends ClassicConverter {

    /**
     * The minimum number of digits a credit card can have.
     */
    private static final int MIN_CC_DIGITS = 13;
    private static final String MASK_LABEL = "MASKED";
    private static final int MASK_LABEL_LENGTH = MASK_LABEL.length();
    private static final String[] MASK_LOOKUPS;
    private static final int MASK_LOOKUPS_SIZE = 20;

    static {
        MASK_LOOKUPS = new String[MASK_LOOKUPS_SIZE];
        for (int i = 0; i < MASK_LOOKUPS.length; i++) {
            MASK_LOOKUPS[i] = buildMask(i);
        }
    }

    @Override
    public String convert(ILoggingEvent e) {
        return mask(e.getFormattedMessage());
    }

    static String mask(String formattedMessage) {
        if (!hasEnoughDigits(formattedMessage)) {
            return formattedMessage;
        }

        int length = formattedMessage.length();
        int unwrittenStart = 0;
        int numberStart = -1;
        int numberEnd;
        int digitsSeen = 0;
        int[] last4pos = {-1, -1, -1, -1};
        int pos;
        char current;

        StringBuilder masked = new StringBuilder(formattedMessage.length());

        for (pos = 0; pos < length; pos++) {
            current = formattedMessage.charAt(pos);
            if (isDigit(current)) {
                digitsSeen++;

                if (numberStart == -1) {
                    numberStart = pos;
                }

                last4pos[0] = last4pos[1];
                last4pos[1] = last4pos[2];
                last4pos[2] = last4pos[3];
                last4pos[3] = pos;
            } else if (digitsSeen > 0 && current != ' ' && current != '-') {
                numberEnd = last4pos[3] + 1;
                if ((digitsSeen >= MIN_CC_DIGITS)
                        && luhnCheck(stripSeparators(
                                        formattedMessage.substring(numberStart, numberEnd)))) {
                    masked.append(formattedMessage, unwrittenStart, numberStart);
                    masked.append(maskString(
                            formattedMessage.substring(numberStart, numberEnd),
                            formattedMessage.substring(last4pos[0], numberEnd)));
                    masked.append(formattedMessage, last4pos[0], numberEnd);
                    unwrittenStart = numberEnd;
                }
                numberStart = -1;
                digitsSeen = 0;
            }
        }

        if (numberStart != -1 && (digitsSeen >= MIN_CC_DIGITS)
                && luhnCheck(stripSeparators(
                                formattedMessage.substring(numberStart, pos)))) {
            masked.append(formattedMessage, unwrittenStart, numberStart);
            masked.append(maskString(
                    formattedMessage.substring(numberStart, pos),
                    formattedMessage.substring(last4pos[0], pos)));
            masked.append(formattedMessage, last4pos[0], pos);
        } else {
            masked.append(formattedMessage, unwrittenStart, pos);
        }

        return masked.toString();
    }

    static boolean hasEnoughDigits(String formattedMessage) {
    	if (formattedMessage == null)
    		return false;
    	
        int digits = 0;
        int length = formattedMessage.length();
        char current;

        for (int i = 0; i < length; i++) {
            current = formattedMessage.charAt(i);
            if (isDigit(current)) {
                if (++digits == MIN_CC_DIGITS) {
                    return true;
                }
            } else if (digits > 0 && current != ' ' && current != '-') {
                digits = 0;
            }
        }

        return false;
    }

    /**
     * Implementation of the [Luhn algorithm](http://en.wikipedia.org/wiki/Luhn_algorithm)
     * to check if the given string is possibly a credit card number.
     *
     * @param cardNumber the number to check. It must only contain numeric characters
     * @return `true` if the given string is a possible credit card number
     */
    static boolean luhnCheck(final String cardNumber) {
        int sum = 0;
        int digit, addend;
        boolean doubled = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (doubled) {
                addend = digit * 2;
                if (addend > 9) {
                    addend -= 9;
                }
            } else {
                addend = digit;
            }
            sum += addend;
            doubled = !doubled;
        }
        return (sum % 10) == 0;
    }

    /**
     * Remove any ` ` and `-` characters from the given string.
     *
     * @param cardNumber the number to clean up
     * @return if the given string contains no ` ` or `-` characters, the string
     *      itself is returned, otherwise a new string containing no ` ` or `-`
     *      characters is returned
     */
    static String stripSeparators(final String cardNumber) {
        final int length = cardNumber.length();
        final char[] result = new char[length];
        int count = 0;
        char cur;
        for (int i = 0; i < length; i++) {
            cur = cardNumber.charAt(i);
            if (!(cur == ' ' || cur == '-')) {
                result[count++] = cur;
            }
        }
        if (count == length) {
            return cardNumber;
        }
        return new String(result, 0, count);
    }

    /**
     * Get a mask string for masking the given `fullNum`.
     *
     * @param fullNum the string to be masked
     * @param unmasked the section of `fullNum` to be left unmasked
     * @return a mask string
     */
    static String maskString(String fullNum, String unmasked) {
        final int maskedLength = fullNum.length() - unmasked.length();
        if (maskedLength < MASK_LOOKUPS_SIZE) {
            return MASK_LOOKUPS[maskedLength];
        } else {
            return buildMask(maskedLength);
        }
    }

    /**
     * Create a masking string with the given length. Masks for short lengths
     * are cached at class initialization to minimize calls to this method.
     *
     * @param maskedLength
     * @return a mask string
     */
    static String buildMask(int maskedLength) {
        final int pads = maskedLength - MASK_LABEL_LENGTH;
        StringBuilder mask = new StringBuilder(maskedLength);
        if (pads <= 0) {
            mask.append(MASK_LABEL);
        } else {
            for (int i = 0; i < pads / 2; i++) {
                mask.append('*');
            }
            mask.append(MASK_LABEL);
            while (mask.length() < maskedLength) {
                mask.append('*');
            }
        }
        return mask.toString();
    }

    private static boolean isDigit(char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return false;
        }
    }
}
