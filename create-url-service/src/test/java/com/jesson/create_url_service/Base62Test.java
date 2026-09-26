package com.jesson.create_url_service;

import com.jesson.create_url_service.utils.Base62;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base62Test {
    private final Base62 base62 = new Base62();

    @Test
    void testEncodeBase62() {
        assertEquals("0", base62.encodeBase62(0L));
        assertEquals("1", base62.encodeBase62(1L));
        assertEquals("Z", base62.encodeBase62(61L));
        assertEquals("10", base62.encodeBase62(62L));
        assertEquals("ZZ", base62.encodeBase62(3843L));
        assertEquals("ZZZ", base62.encodeBase62(238327L));
    }
}
