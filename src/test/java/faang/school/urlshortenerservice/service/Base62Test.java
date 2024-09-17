package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class Base62Test {
    @InjectMocks
    private Base62 base62;

    @Test
    void encode() {
        List<Long> numbers = Arrays.asList(1L,2L,3L,123L);
        List<Hash> expected = Arrays.asList(new Hash("1"),new Hash("2"), new Hash("3"), new Hash("z1"));
        Assertions.assertEquals(expected, base62.encode(numbers));
    }
}