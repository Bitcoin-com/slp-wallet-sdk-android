/*
 * Copyright 2018 the bitcoinj-cash developers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitcoin.slpwallet.bitcoinj;

import com.bitcoin.slpwallet.address.AddressFormatException;

/**
 * From BitcoinJ
 */
class AddressCashValidator {

    static void checkNonEmptyPayload(byte[] payload) {
        if (payload.length == 0) {
            throw new AddressFormatException("No payload");
        }
    }

    static void checkAllowedPadding(byte extraBits) {
        if (extraBits >= 5) {
            throw new AddressFormatException("More than allowed padding");
        }
    }

    static void checkNonZeroPadding(byte last, byte mask) {
        if ((last & mask) != 0) {
            throw new AddressFormatException("Nonzero padding bytes");
        }
    }

    static void checkFirstBitIsZero(byte versionByte) {
        if ((versionByte & 0x80) != 0) {
            throw new AddressFormatException("First bit is reserved");
        }
    }

    static void checkDataLength(byte[] data, int hashSize) {
        if (data.length != hashSize + 1) {
            throw new AddressFormatException("Data length " + data.length + " != hash size " + hashSize);
        }
    }

}
