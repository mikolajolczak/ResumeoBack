package com.example.resumeoback.position;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "http://localhost:4200")
public class PositionController {

    private final List<Position> positions = new ArrayList<>();

    public String getRandomPosition() {
        if (positions.isEmpty()) {
            return "Brak dostępnych stanowisk";
        }
        Random random = new Random();
        int index = random.nextInt(positions.size());
        return positions.get(index).getName();
    }

    public PositionController() {
        positions.add(new Position(UUID.randomUUID().toString(), "Junior Developer", 3, "2024-06-01"));
        positions.add(new Position(UUID.randomUUID().toString(), "Project Manager", 2, "2024-06-02"));
        positions.add(new Position(UUID.randomUUID().toString(), "UX Designer", 1, "2024-06-03"));
    }

    @GetMapping
    public List<Position> getAllPositions() {
        return positions;
    }

    @PostMapping
    public Position addPosition(@RequestBody Position position) {
        position.setId(UUID.randomUUID().toString());
        positions.add(position);
        return position;
    }

    @DeleteMapping("/{id}")
    public void deletePosition(@PathVariable String id) {
        positions.removeIf(p -> p.getId().equals(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable String id, @RequestBody Position updatedPosition) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i).getId().equals(id)) {
                updatedPosition.setId(id);
                positions.set(i, updatedPosition);
                return ResponseEntity.ok(updatedPosition);
            }
        }
        return ResponseEntity.notFound().build();
    }
}