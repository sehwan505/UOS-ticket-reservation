package com.example.backend.controller;

import com.example.backend.dto.CinemaDto;
import com.example.backend.dto.MovieDto;
import com.example.backend.dto.ScheduleDto;
import com.example.backend.service.CinemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
@Tag(name = "Cinema", description = "영화관 관련 API")
public class CinemaController {
    
    private final CinemaService cinemaService;
    
    // 모든 영화관 조회
    @GetMapping
    @Operation(
        summary = "영화관 목록 조회",
        description = "모든 영화관의 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화관 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CinemaDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    [
                        {
                            "id": "01",
                            "name": "강남점",
                            "location": "서울시 강남구 테헤란로 123",
                            "regionId": "01",
                            "regionName": "서울",
                            "screenCount": 5
                        },
                        {
                            "id": "02",
                            "name": "홍대점", 
                            "location": "서울시 마포구 홍익로 456",
                            "regionId": "01",
                            "regionName": "서울",
                            "screenCount": 4
                        }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<CinemaDto>> getAllCinemas() {
        List<CinemaDto> cinemas = cinemaService.findAllCinemas();
        return ResponseEntity.ok(cinemas);
    }
    
    // 지역별 영화관 조회
    @GetMapping("/regions/{regionId}")
    @Operation(
        summary = "지역별 영화관 조회",
        description = "특정 지역의 영화관 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "지역별 영화관 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CinemaDto.class)
            )
        )
    })
    public ResponseEntity<List<CinemaDto>> getCinemasByRegion(
            @Parameter(description = "지역 ID", required = true)
            @PathVariable String regionId) {
        List<CinemaDto> cinemas = cinemaService.findCinemasByRegion(regionId);
        return ResponseEntity.ok(cinemas);
    }
    
    // 영화관 상세 조회
    @GetMapping("/{cinemaId}")
    @Operation(
        summary = "영화관 상세 조회",
        description = "특정 영화관의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화관 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CinemaDto.class)
            )
        )
    })
    public ResponseEntity<CinemaDto> getCinema(
            @Parameter(description = "영화관 ID", required = true)
            @PathVariable String cinemaId) {
        CinemaDto cinema = cinemaService.findCinemaById(cinemaId);
        return ResponseEntity.ok(cinema);
    }


    @GetMapping("/{cinemaId}/movies/current")
    @Operation(
        summary = "영화관별 현재 상영중인 영화 조회",
        description = "특정 영화관에서 현재 날짜 이후에 스케줄이 있는 상영중인 영화 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "현재 상영중인 영화 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "cinema": {
                            "id": "01",
                            "name": "강남점"
                        },
                        "movies": [
                            {
                                "id": 1,
                                "title": "아바타: 물의 길",
                                "genre": "SF",
                                "runtime": 192,
                                "rating": 4.5
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getCurrentMoviesByCinema(
            @Parameter(description = "영화관 ID", required = true)
            @PathVariable String cinemaId) {
        
        CinemaDto cinema = cinemaService.findCinemaById(cinemaId);
        List<MovieDto> movies = cinemaService.findCurrentMoviesByCinema(cinemaId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cinema", cinema);
        response.put("movies", movies);
        
        return ResponseEntity.ok(response);
    }
    
    // 영화관별 특정 날짜 스케줄 조회
    @GetMapping("/{cinemaId}/schedules/dates/{date}")
    @Operation(
        summary = "영화관별 특정 날짜 스케줄 조회",
        description = "특정 영화관에서 특정 날짜의 모든 상영 스케줄을 시간순으로 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화관별 특정 날짜 스케줄 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "cinema": {
                            "id": "01",
                            "name": "강남점"
                        },
                        "date": "20240101",
                        "schedules": [
                            {
                                "id": "2024010101001",
                                "movieId": 1,
                                "movieTitle": "아바타: 물의 길",
                                "screenId": "01",
                                "screenName": "1관",
                                "screeningDate": "20240101",
                                "screeningStartTime": "2024-01-01T09:30:00"
                            },
                            {
                                "id": "2024010102001",
                                "movieId": 2,
                                "movieTitle": "탑건: 매버릭",
                                "screenId": "02",
                                "screenName": "2관",
                                "screeningDate": "20240101",
                                "screeningStartTime": "2024-01-01T12:00:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getSchedulesByCinemaAndDate(
            @Parameter(description = "영화관 ID", required = true)
            @PathVariable String cinemaId,
            @Parameter(description = "상영 날짜 (YYYYMMDD)", required = true)
            @PathVariable String date) {
        
        CinemaDto cinema = cinemaService.findCinemaById(cinemaId);
        List<ScheduleDto> schedules = cinemaService.findSchedulesByCinemaAndDate(cinemaId, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cinema", cinema);
        response.put("date", date);
        response.put("schedules", schedules);
        
        return ResponseEntity.ok(response);
    }
    
    // 특정 영화관에서 특정 영화의 모든 스케줄 조회
    @GetMapping("/{cinemaId}/movies/{movieId}/schedules")
    @Operation(
        summary = "특정 영화관에서 특정 영화의 모든 스케줄 조회",
        description = "특정 영화관에서 특정 영화의 모든 상영 스케줄을 날짜와 시간순으로 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "특정 영화관에서 특정 영화의 스케줄 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "cinema": {
                            "id": "01",
                            "name": "강남점",
                            "location": "서울시 강남구",
                            "regionId": "SEOUL",
                            "regionName": "서울",
                            "screenCount": 10
                        },
                        "movieId": 1,
                        "schedules": [
                            {
                                "id": "2024010101001",
                                "movieId": 1,
                                "movieTitle": "아바타: 물의 길",
                                "screenId": "01",
                                "screenName": "1관",
                                "screeningDate": "20240101",
                                "screeningStartTime": "2024-01-01T09:30:00"
                            },
                            {
                                "id": "2024010201001",
                                "movieId": 1,
                                "movieTitle": "아바타: 물의 길",
                                "screenId": "02",
                                "screenName": "2관",
                                "screeningDate": "20240102",
                                "screeningStartTime": "2024-01-02T14:00:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getSchedulesByCinemaAndMovie(
            @Parameter(description = "영화관 ID", required = true)
            @PathVariable String cinemaId,
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long movieId) {
        
        CinemaDto cinema = cinemaService.findCinemaById(cinemaId);
        List<ScheduleDto> schedules = cinemaService.findSchedulesByCinemaAndMovie(cinemaId, movieId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cinema", cinema);
        response.put("movieId", movieId);
        response.put("schedules", schedules);
        
        return ResponseEntity.ok(response);
    }
    
    // 특정 영화관에서 특정 영화의 특정 날짜 스케줄 조회
    @GetMapping("/{cinemaId}/movies/{movieId}/schedules/dates/{date}")
    @Operation(
        summary = "특정 영화관에서 특정 영화의 특정 날짜 스케줄 조회",
        description = "특정 영화관에서 특정 영화의 특정 날짜 상영 스케줄을 시간순으로 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "특정 영화관에서 특정 영화의 특정 날짜 스케줄 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "cinema": {
                            "id": "01",
                            "name": "강남점",
                            "location": "서울시 강남구",
                            "regionId": "SEOUL",
                            "regionName": "서울",
                            "screenCount": 10
                        },
                        "movieId": 1,
                        "date": "20240101",
                        "schedules": [
                            {
                                "id": "2024010101001",
                                "movieId": 1,
                                "movieTitle": "아바타: 물의 길",
                                "screenId": "01",
                                "screenName": "1관",
                                "screeningDate": "20240101",
                                "screeningStartTime": "2024-01-01T09:30:00"
                            },
                            {
                                "id": "2024010102001",
                                "movieId": 1,
                                "movieTitle": "아바타: 물의 길",
                                "screenId": "02",
                                "screenName": "2관",
                                "screeningDate": "20240101",
                                "screeningStartTime": "2024-01-01T12:00:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getSchedulesByCinemaAndMovieAndDate(
            @Parameter(description = "영화관 ID", required = true)
            @PathVariable String cinemaId,
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long movieId,
            @Parameter(description = "상영 날짜 (YYYYMMDD)", required = true)
            @PathVariable String date) {
        
        CinemaDto cinema = cinemaService.findCinemaById(cinemaId);
        List<ScheduleDto> schedules = cinemaService.findSchedulesByCinemaAndMovieAndDate(cinemaId, movieId, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cinema", cinema);
        response.put("movieId", movieId);
        response.put("date", date);
        response.put("schedules", schedules);
        
        return ResponseEntity.ok(response);
    }
} 