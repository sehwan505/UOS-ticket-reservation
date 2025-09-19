package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.ScreenEntity;
import sehwan505.uosticketreservation.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<SeatEntity, Integer> {
    
    List<SeatEntity> findByScreen(ScreenEntity screen);
    
    List<SeatEntity> findByScreenOrderByRowAscColumnAsc(ScreenEntity screen);
    
    // 관리자 기능용 메서드들
    boolean existsByScreenAndRowAndColumn(ScreenEntity screen, String row, String column);
    
    int countByScreen(ScreenEntity screen);
}