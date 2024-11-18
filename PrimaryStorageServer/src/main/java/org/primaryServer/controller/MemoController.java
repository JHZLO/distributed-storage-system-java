package org.primaryServer.controller;

import java.util.List;
import org.primaryServer.domain.Memo;
import org.primaryServer.service.MemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/primary")
public class MemoController {
    private final MemoService memoService;

    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }

    // GET /primary - 모든 메모 조회
    @GetMapping
    public List<Memo> getAllMemos() {
        return memoService.getAllMemos();
    }

    // GET /primary/{id} - 특정 메모 조회
    @GetMapping("/{id}")
    public ResponseEntity<Memo> getMemoById(@PathVariable Long id) {
        return memoService.getMemoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /primary - 새 메모 생성
    @PostMapping
    public Memo createMemo(@RequestBody Memo memo) {
        return memoService.createMemo(memo);
    }

    // PUT /primary/{id} - 기존 메모 수정
    @PutMapping("/{id}")
    public ResponseEntity<Memo> updateMemo(@PathVariable Long id, @RequestBody Memo memo) {
        try {
            Memo updatedMemo = memoService.updateMemo(id, memo);
            return ResponseEntity.ok(updatedMemo);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PATCH /primary/{id} - 기존 메모의 일부 필드 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Memo> partialUpdateMemo(@PathVariable Long id, @RequestBody Memo memo) {
        try {
            Memo updatedMemo = memoService.partialUpdateMemo(id, memo);
            return ResponseEntity.ok(updatedMemo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // DELETE /primary/{id} - 특정 메모 삭제
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
