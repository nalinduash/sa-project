package com.bookfair.repository;

import com.bookfair.model.Stall;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Focuses only on the Database layer (Fast & Efficient)
public class StallRepositoryTest {

    @Autowired
    private StallRepository stallRepository;

    @Test
    void testFindByIsAvailableTrue() {
        // Limit: Only test if it filters out reserved stalls
        stallRepository.save(Stall.builder().stallCode("A1").isAvailable(true).build());
        stallRepository.save(Stall.builder().stallCode("A2").isAvailable(false).build());

        List<Stall> results = stallRepository.findByIsAvailableTrue();

        assertEquals(1, results.size());
        assertEquals("A1", results.get(0).getStallCode());
    }

    @Test
    void testFindAllSortedByMapPosition() {
        // Limit: Only test if the Grid order is correct for the frontend map
        stallRepository.save(Stall.builder().stallCode("B1").rowPosition(1).columnPosition(0).build());
        stallRepository.save(Stall.builder().stallCode("A1").rowPosition(0).columnPosition(0).build());

        List<Stall> results = stallRepository.findAllByOrderByRowPositionAscColumnPositionAsc();

        assertEquals("A1", results.get(0).getStallCode()); // Row 0 should come before Row 1
    }
}