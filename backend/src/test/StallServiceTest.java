package com.bookfair.service;

import com.bookfair.repository.StallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StallServiceTest {

    @Mock
    private StallRepository stallRepository;

    @InjectMocks
    private StallService stallService;

    @Test
    void testInitializeStalls_Creates40Stalls() {
        // Limit: Test if DB is empty, it saves 40 stalls (5x8 grid)
        when(stallRepository.count()).thenReturn(0L);
        stallService.initializeStalls();
        verify(stallRepository, times(40)).save(any());
    }

    @Test
    void testInitializeStalls_DoesNothingIfNotEmpty() {
        // Limit: Test efficiency - don't duplicate data
        when(stallRepository.count()).thenReturn(10L);
        stallService.initializeStalls();
        verify(stallRepository, never()).save(any());
    }
}