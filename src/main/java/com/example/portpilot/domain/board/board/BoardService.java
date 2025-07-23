package com.example.portpilot.domain.board.board;

import com.example.portpilot.domain.user.User;
import com.example.portpilot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void create(BoardRequestDto dto, User user) {
        Board board = dto.toEntity();
        board.setUser(user);
        boardRepository.save(board);
    }

    public Page<BoardResponseDto> getBoardsFiltered(String jobType, String techStack, String keyword, Pageable pageable) {
        // 빈 문자열을 null 처리
        String safeJobType = (jobType == null || jobType.trim().isEmpty()) ? null : jobType;
        String safeTechStack = (techStack == null || techStack.trim().isEmpty()) ? null : techStack;
        String safeKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword;

        Page<Board> boards = boardRepository.findByFilter(safeJobType, safeTechStack, safeKeyword, pageable);
        return boards.map(BoardResponseDto::fromEntity);
    }

    @Transactional
    public BoardResponseDto viewBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        board.setViewCount(board.getViewCount() + 1);
        return BoardResponseDto.fromEntity(board);
    }

    public BoardResponseDto getById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return BoardResponseDto.fromEntity(board);
    }

    @Transactional
    public void updateBoard(Long id, BoardRequestDto dto, User user) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!board.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        board.setTitle(dto.getTitle());
        board.setContent(dto.getContent());
        board.setJobType(dto.getJobType());
        board.setTechStack(dto.getTechStack());
    }

    @Transactional
    public void deleteBoard(Long id, User user) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!board.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        boardRepository.delete(board);
    }

    public Page<BoardResponseDto> getMyBoards(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email);
        Page<Board> boards = boardRepository.findByUserId(user.getId(), pageable);
        return boards.map(BoardResponseDto::fromEntity);
    }
}
