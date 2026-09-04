package com.jesson.create_url_service;

import com.jesson.create_url_service.utils.Base62;
import org.junit.jupiter.api.Test;

public class Base62Test {
    private final Base62 base62 = new Base62();

    @Test
    void testEncodeBase62() {
        assert base62.encodeBase62(0L).equals("0");
        assert base62.encodeBase62(1L).equals("1");
        assert base62.encodeBase62(61L).equals("Z");
        assert base62.encodeBase62(62L).equals("10");
        assert base62.encodeBase62(3843L).equals("ZZ");
        assert base62.encodeBase62(238327L).equals("ZZZ");
    }

}
