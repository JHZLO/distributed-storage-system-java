package org.example.controller;

import org.example.domain.Memo;
import org.example.service.MemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class MemoController {
    private final MemoService memoService;

    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }

    // GET /notes - 모든 메모 조회
    @GetMapping
    public List<Memo> getAllMemos() {
        return memoService.getAllMemos();
    }

    // GET /notes/{id} - 특정 메모 조회
    @GetMapping("/{id}")
    public ResponseEntity<Memo> getMemoById(@PathVariable Long id) {
        return memoService.getMemoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /notes - 새 메모 생성
    @PostMapping
    public Memo createMemo(@RequestBody Memo memo) {
        return memoService.createMemo(memo);
    }

    // PUT /notes/{id} - 기존 메모 수정
    @PutMapping("/{id}")
    public ResponseEntity<Memo> updateMemo(@PathVariable Long id, @RequestBody Memo memo) {
        try {
            Memo updatedMemo = memoService.updateMemo(id, memo);
            return ResponseEntity.ok(updatedMemo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /notes/{id} - 기존 메모의 일부 필드 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Memo> partialUpdateMemo(@PathVariable Long id, @RequestBody Memo memo) {
        try {
            Memo updatedMemo = memoService.partialUpdateMemo(id, memo);
            return ResponseEntity.ok(updatedMemo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // DELETE /notes/{id} - 특정 메모 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMemo(@PathVariable Long id) {
        try {
            memoService.deleteMemo(id);
            return ResponseEntity.ok().body("{\"msg\": \"OK\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("{\"msg\": \"ERROR\"}");
        }
    }
}
