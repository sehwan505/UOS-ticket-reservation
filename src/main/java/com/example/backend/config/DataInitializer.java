package com.example.backend.config;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 개발 환경에서만 실행 (프로파일 체크 가능)
        if (movieRepository.count() == 0) {
            log.info("🎬 더미 데이터 생성을 시작합니다...");
            
            initializeBasicData();
            initializeMovies();
            initializeSchedules();
            initializeMembers();
            initializeReservations();
            initializeReviews();
            
            log.info("✅ 더미 데이터 생성이 완료되었습니다!");
        } else {
            log.info("📊 기존 데이터가 존재하여 더미 데이터 생성을 건너뜁니다.");
        }
    }

    private void initializeBasicData() {
        // 1. 지역 데이터 (확장)
        if (regionRepository.count() == 0) {
            List<RegionEntity> regions = List.of(
                RegionEntity.builder().id("01").name("서울").build(),
                RegionEntity.builder().id("02").name("경기").build(),
                RegionEntity.builder().id("03").name("부산").build(),
                RegionEntity.builder().id("04").name("대구").build(),
                RegionEntity.builder().id("05").name("인천").build(),
                RegionEntity.builder().id("06").name("광주").build(),
                RegionEntity.builder().id("07").name("대전").build(),
                RegionEntity.builder().id("08").name("울산").build(),
                RegionEntity.builder().id("09").name("강원").build(),
                RegionEntity.builder().id("10").name("충북").build()
            );
            regionRepository.saveAll(regions);
            log.info("📍 지역 데이터 생성 완료: {}개", regions.size());
        }

        // 2. 영화관 데이터 (확장)
        if (cinemaRepository.count() == 0) {
            RegionEntity seoul = regionRepository.findById("01").orElseThrow();
            RegionEntity gyeonggi = regionRepository.findById("02").orElseThrow();
            RegionEntity busan = regionRepository.findById("03").orElseThrow();
            RegionEntity daegu = regionRepository.findById("04").orElseThrow();
            RegionEntity incheon = regionRepository.findById("05").orElseThrow();
            
            List<CinemaEntity> cinemas = List.of(
                // 서울
                CinemaEntity.builder().id("01").name("강남점").location("서울시 강남구 테헤란로 123").region(seoul).build(),
                CinemaEntity.builder().id("02").name("홍대점").location("서울시 마포구 홍익로 45").region(seoul).build(),
                CinemaEntity.builder().id("03").name("잠실점").location("서울시 송파구 올림픽로 240").region(seoul).build(),
                CinemaEntity.builder().id("04").name("명동점").location("서울시 중구 명동길 26").region(seoul).build(),
                CinemaEntity.builder().id("05").name("신촌점").location("서울시 서대문구 신촌로 83").region(seoul).build(),
                
                // 경기
                CinemaEntity.builder().id("06").name("수원점").location("경기도 수원시 영통구 월드컵로 206").region(gyeonggi).build(),
                CinemaEntity.builder().id("07").name("분당점").location("경기도 성남시 분당구 판교역로 146").region(gyeonggi).build(),
                CinemaEntity.builder().id("08").name("일산점").location("경기도 고양시 일산서구 중앙로 1455").region(gyeonggi).build(),
                
                // 부산
                CinemaEntity.builder().id("09").name("센텀시티점").location("부산시 해운대구 센텀중앙로 79").region(busan).build(),
                CinemaEntity.builder().id("10").name("서면점").location("부산시 부산진구 중앙대로 691").region(busan).build(),
                
                // 대구
                CinemaEntity.builder().id("11").name("동성로점").location("대구시 중구 동성로2길 81").region(daegu).build(),
                
                // 인천
                CinemaEntity.builder().id("12").name("송도점").location("인천시 연수구 센트럴로 123").region(incheon).build()
            );
            cinemaRepository.saveAll(cinemas);
            log.info("🏢 영화관 데이터 생성 완료: {}개", cinemas.size());
        }

        // 3. 상영관 데이터 (확장)
        if (screenRepository.count() == 0) {
            List<CinemaEntity> cinemas = cinemaRepository.findAll();
            List<ScreenEntity> screens = new ArrayList<>();
            
            int screenIdCounter = 1; // 전체 상영관에 대한 순차적 ID
            
            for (CinemaEntity cinema : cinemas) {
                // 각 영화관마다 3-5개의 상영관 생성
                int screenCount = 3 + random.nextInt(3); // 3-5개
                for (int i = 1; i <= screenCount; i++) {
                    int totalSeats = 80 + random.nextInt(61); // 80-140석
                    screens.add(ScreenEntity.builder()
                        .id(String.format("%02d", screenIdCounter++)) // 2자리 순차 ID
                        .name(i + "관")
                        .totalSeats(totalSeats)
                        .cinema(cinema)
                        .build());
                }
            }
            
            screenRepository.saveAll(screens);
            log.info("🎭 상영관 데이터 생성 완료: {}개", screens.size());
        }

        // 4. 좌석 등급 데이터
        if (seatGradeRepository.count() == 0) {
            List<SeatGradeEntity> seatGrades = List.of(
                SeatGradeEntity.builder().id("A").name("일반석").price(12000).build(),
                SeatGradeEntity.builder().id("B").name("프리미엄").price(15000).build(),
                SeatGradeEntity.builder().id("C").name("커플석").price(18000).build(),
                SeatGradeEntity.builder().id("D").name("VIP석").price(22000).build()
            );
            seatGradeRepository.saveAll(seatGrades);
            log.info("💺 좌석 등급 데이터 생성 완료: {}개", seatGrades.size());
        }

        // 5. 좌석 데이터 (모든 상영관에 대해 생성)
        if (seatRepository.count() == 0) {
            List<ScreenEntity> screens = screenRepository.findAll();
            List<SeatGradeEntity> seatGrades = seatGradeRepository.findAll();
            List<SeatEntity> seats = new ArrayList<>();
            
            for (ScreenEntity screen : screens) {
                generateSeatsForScreen(screen, seatGrades, seats);
            }
            
            seatRepository.saveAll(seats);
            log.info("🪑 좌석 데이터 생성 완료: {}개", seats.size());
        }
    }

    private void generateSeatsForScreen(ScreenEntity screen, List<SeatGradeEntity> seatGrades, List<SeatEntity> seats) {
        int totalSeats = screen.getTotalSeats();
        int seatsPerRow = 15; // 기본 한 줄당 15석
        int rows = (totalSeats + seatsPerRow - 1) / seatsPerRow; // 올림 계산
        
        SeatGradeEntity gradeA = seatGrades.get(0); // 일반석
        SeatGradeEntity gradeB = seatGrades.get(1); // 프리미엄
        SeatGradeEntity gradeC = seatGrades.get(2); // 커플석
        SeatGradeEntity gradeD = seatGrades.get(3); // VIP석
        
        int seatCount = 0;
        for (int row = 0; row < rows && seatCount < totalSeats; row++) {
            char rowChar = (char) ('A' + row);
            int seatsInThisRow = Math.min(seatsPerRow, totalSeats - seatCount);
            
            // 좌석 등급 결정 (뒤쪽이 더 좋은 등급)
            SeatGradeEntity grade;
            if (row < rows * 0.4) {
                grade = gradeA; // 앞쪽 40%: 일반석
            } else if (row < rows * 0.7) {
                grade = gradeB; // 중간 30%: 프리미엄
            } else if (row < rows * 0.9) {
                grade = gradeD; // 뒤쪽 20%: VIP석
            } else {
                grade = gradeC; // 맨 뒤 10%: 커플석
                seatsInThisRow = Math.min(seatsInThisRow, 10); // 커플석은 최대 10석
            }
            
            for (int col = 1; col <= seatsInThisRow; col++) {
                seats.add(SeatEntity.builder()
                    .row(String.valueOf(rowChar))
                    .column(String.format("%02d", col))
                    .screen(screen)
                    .seatGrade(grade)
                    .build());
                seatCount++;
            }
        }
    }

    private void initializeMovies() {
        List<MovieEntity> movies = List.of(
            // 최신 블록버스터
            MovieEntity.builder()
                .title("아바타: 물의 길")
                .genre("SF")
                .releaseDate("20221214")
                .screeningStatus("D")
                .runtime(192)
                .actorName("샘 워딩턴, 조 샐다나")
                .directorName("제임스 카메론")
                .distributorName("월트디즈니")
                .viewingGrade("12")
                .description("판도라 행성에서 펼쳐지는 새로운 모험. 제이크 설리와 네이티리 가족의 이야기가 계속됩니다.")
                .image("/images/avatar2.jpg")
                .rating(4.5)
                .build(),
                
            MovieEntity.builder()
                .title("탑건: 매버릭")
                .genre("AC")
                .releaseDate("20220622")
                .screeningStatus("D")
                .runtime(131)
                .actorName("톰 크루즈, 마일스 텔러")
                .directorName("조셉 코신스키")
                .distributorName("파라마운트")
                .viewingGrade("12")
                .description("전설적인 파일럿 매버릭의 귀환. 새로운 임무와 함께 과거와 마주하게 됩니다.")
                .image("/images/topgun.jpg")
                .rating(4.7)
                .build(),
                
            MovieEntity.builder()
                .title("블랙 팬서: 와칸다 포에버")
                .genre("AC")
                .releaseDate("20221109")
                .screeningStatus("D")
                .runtime(161)
                .actorName("안젤라 바셋, 테노치 우에르타")
                .directorName("라이언 쿠글러")
                .distributorName("월트디즈니")
                .viewingGrade("12")
                .description("와칸다를 지키기 위한 새로운 전쟁. 티찰라 없는 와칸다의 미래를 그립니다.")
                .image("/images/blackpanther2.jpg")
                .rating(4.2)
                .build(),
                
            // 애니메이션
            MovieEntity.builder()
                .title("미니언즈: 라이즈 오브 그루")
                .genre("AN")
                .releaseDate("20220720")
                .screeningStatus("D")
                .runtime(87)
                .actorName("스티브 카렐, 피에르 코팽")
                .directorName("카일 발다")
                .distributorName("유니버설")
                .viewingGrade("전체")
                .description("그루의 어린 시절 이야기. 미니언즈와의 첫 만남을 그린 프리퀄입니다.")
                .image("/images/minions.jpg")
                .rating(4.0)
                .build(),
                
            MovieEntity.builder()
                .title("엔칸토: 마법의 세계")
                .genre("AN")
                .releaseDate("20211124")
                .screeningStatus("D")
                .runtime(102)
                .actorName("스테파니 베아트리즈, 마리아 세실리아")
                .directorName("바이런 하워드")
                .distributorName("월트디즈니")
                .viewingGrade("전체")
                .description("마법의 힘을 잃어버린 소녀 미라벨의 모험을 그린 디즈니 애니메이션입니다.")
                .image("/images/encanto.jpg")
                .rating(4.3)
                .build(),
                
            // 한국 영화
            MovieEntity.builder()
                .title("헤어질 결심")
                .genre("DR")
                .releaseDate("20220629")
                .screeningStatus("D")
                .runtime(138)
                .actorName("박해일, 탕웨이")
                .directorName("박찬욱")
                .distributorName("CJ ENM")
                .viewingGrade("15")
                .description("산 위에서 추락한 남자의 죽음을 수사하던 형사가 그의 아내를 만나면서 벌어지는 이야기입니다.")
                .image("/images/decision_to_leave.jpg")
                .rating(4.4)
                .build(),
                
            MovieEntity.builder()
                .title("범죄도시 3")
                .genre("AC")
                .releaseDate("20230531")
                .screeningStatus("D")
                .runtime(105)
                .actorName("마동석, 이준혁")
                .directorName("이상용")
                .distributorName("빅펀치픽쳐스")
                .viewingGrade("15")
                .description("마석도가 신종 마약 사건을 해결하기 위해 벌이는 통쾌한 액션 수사극입니다.")
                .image("/images/crime_city3.jpg")
                .rating(4.1)
                .build(),
                
            // 호러/스릴러
            MovieEntity.builder()
                .title("스크림 6")
                .genre("HO")
                .releaseDate("20230310")
                .screeningStatus("D")
                .runtime(123)
                .actorName("멜리사 바레라, 제나 오르테가")
                .directorName("맷 베티넬리-올핀")
                .distributorName("파라마운트")
                .viewingGrade("18")
                .description("고스트페이스 킬러가 다시 돌아왔습니다. 새로운 살인 사건이 시작됩니다.")
                .image("/images/scream6.jpg")
                .rating(3.8)
                .build(),
                
            // 로맨스
            MovieEntity.builder()
                .title("어 굿 퍼슨")
                .genre("RO")
                .releaseDate("20230915")
                .screeningStatus("D")
                .runtime(129)
                .actorName("플로렌스 퓨, 모건 프리먼")
                .directorName("잭 브래프")
                .distributorName("라이온스게이트")
                .viewingGrade("15")
                .description("완벽한 삶을 살던 여성이 예상치 못한 사건으로 인해 인생이 바뀌는 이야기입니다.")
                .image("/images/a_good_person.jpg")
                .rating(3.9)
                .build(),
                
            // 상영 종료된 영화들
            MovieEntity.builder()
                .title("닥터 스트레인지: 대혼돈의 멀티버스")
                .genre("AC")
                .releaseDate("20220504")
                .screeningStatus("Y")
                .runtime(126)
                .actorName("베네딕트 컴버배치, 엘리자베스 올슨")
                .directorName("샘 레이미")
                .distributorName("월트디즈니")
                .viewingGrade("12")
                .description("멀티버스의 문이 열리면서 벌어지는 혼돈과 모험을 그린 마블 영화입니다.")
                .image("/images/doctorstrange2.jpg")
                .rating(4.1)
                .build(),
                
            MovieEntity.builder()
                .title("쥬라기 월드: 도미니언")
                .genre("AC")
                .releaseDate("20220601")
                .screeningStatus("Y")
                .runtime(147)
                .actorName("크리스 프랫, 브라이스 달라스 하워드")
                .directorName("콜린 트레보로우")
                .distributorName("유니버설")
                .viewingGrade("12")
                .description("공룡과 인간이 공존하는 세상에서 벌어지는 마지막 모험입니다.")
                .image("/images/jurassic_dominion.jpg")
                .rating(3.7)
                .build(),
                
            MovieEntity.builder()
                .title("토르: 러브 앤 썬더")
                .genre("AC")
                .releaseDate("20220706")
                .screeningStatus("Y")
                .runtime(119)
                .actorName("크리스 헴스워스, 나탈리 포트만")
                .directorName("타이카 와이티티")
                .distributorName("월트디즈니")
                .viewingGrade("12")
                .description("토르의 새로운 모험과 사랑 이야기를 그린 마블 영화입니다.")
                .image("/images/thor4.jpg")
                .rating(3.6)
                .build()
        );
        
        movieRepository.saveAll(movies);
        log.info("🎬 영화 데이터 생성 완료: {}편", movies.size());
    }

    private void initializeSchedules() {
        List<MovieEntity> allMovies = movieRepository.findAll();
        List<MovieEntity> movies = allMovies.stream()
            .filter(movie -> "D".equals(movie.getScreeningStatus()))
            .toList();
        List<ScreenEntity> screens = screenRepository.findAll();
        
        if (movies.isEmpty() || screens.isEmpty()) {
            log.warn("⚠️ 영화 또는 상영관 데이터가 없어 스케줄 생성을 건너뜁니다.");
            return;
        }

        List<ScheduleEntity> schedules = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        
        // 오늘부터 14일간의 스케줄 생성
        for (int day = 0; day < 14; day++) {
            String date = LocalDate.now().plusDays(day).format(formatter);
            
            for (ScreenEntity screen : screens) {
                // 각 상영관에 하루 4-6회 상영 (랜덤)
                int dailyScreenings = 4 + random.nextInt(3); // 4-6회
                String[] possibleTimes = {"0930", "1200", "1430", "1700", "1930", "2200"};
                
                // 영화를 랜덤하게 배정
                List<MovieEntity> shuffledMovies = new ArrayList<>(movies);
                java.util.Collections.shuffle(shuffledMovies, random);
                
                for (int i = 0; i < dailyScreenings && i < possibleTimes.length; i++) {
                    MovieEntity movie = shuffledMovies.get(i % shuffledMovies.size());
                    
                    // 상영시작시간을 LocalDateTime으로 변환
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                    LocalDateTime startTime = LocalDateTime.parse(date + possibleTimes[i], timeFormatter);
                    
                    schedules.add(ScheduleEntity.builder()
                        .id(generateScheduleId(date, screen.getId(), movie.getId(), i))
                        .movie(movie)
                        .screen(screen)
                        .screeningDate(date)
                        .screeningStartTime(startTime)
                        .build());
                }
            }
        }
        
        scheduleRepository.saveAll(schedules);
        log.info("📅 상영 스케줄 생성 완료: {}개", schedules.size());
    }

    private void initializeMembers() {
        if (memberRepository.count() == 0) {
            List<MemberEntity> members = new ArrayList<>();
            
            // 테스트 회원
            members.add(MemberEntity.builder()
                .userId("testuser")
                .password(passwordEncoder.encode("password123"))
                .email("test@example.com")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .grade("B")
                .availablePoints(5000)
                .build());
            
            // 관리자 계정
            members.add(MemberEntity.builder()
                .userId("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@cinema.com")
                .phoneNumber("01000000000")
                .birthDate("19800101")
                .grade("P") // Platinum
                .availablePoints(100000)
                .build());
            
            // 일반 회원들
            String[] names = {"김영희", "이철수", "박민수", "최지영", "정다은", "한상우", "오세진", "임나영", "조현우", "강미래"};
            String[] domains = {"gmail.com", "naver.com", "daum.net", "kakao.com", "yahoo.com"};
            String[] grades = {"B", "S", "G", "P"}; // Bronze, Silver, Gold, Platinum
            
            for (int i = 0; i < 50; i++) {
                String name = names[random.nextInt(names.length)];
                String userId = "user" + String.format("%03d", i + 1);
                String email = userId + "@" + domains[random.nextInt(domains.length)];
                String phone = "010" + String.format("%08d", 10000000 + random.nextInt(90000000));
                String birthDate = String.format("%04d%02d%02d", 
                    1970 + random.nextInt(35), // 1970-2004년생
                    1 + random.nextInt(12),     // 1-12월
                    1 + random.nextInt(28));    // 1-28일
                String grade = grades[random.nextInt(grades.length)];
                int points = random.nextInt(50000); // 0-50000 포인트
                
                members.add(MemberEntity.builder()
                    .userId(userId)
                    .password(passwordEncoder.encode("password123"))
                    .email(email)
                    .phoneNumber(phone)
                    .birthDate(birthDate)
                    .grade(grade)
                    .availablePoints(points)
                    .build());
            }
            
            memberRepository.saveAll(members);
            log.info("👤 회원 데이터 생성 완료: {}명", members.size());
        }
    }

    private void initializeReservations() {
        if (reservationRepository.count() == 0) {
            List<MemberEntity> members = memberRepository.findAll();
            List<ScheduleEntity> schedules = scheduleRepository.findAll();
            
            if (members.isEmpty() || schedules.isEmpty()) {
                log.warn("⚠️ 회원 또는 스케줄 데이터가 없어 예약 생성을 건너뜁니다.");
                return;
            }
            
            List<ReservationEntity> reservations = new ArrayList<>();
            
            // 과거 예약들 생성 (지난 7일간)
            for (int day = 7; day >= 1; day--) {
                LocalDate reservationDate = LocalDate.now().minusDays(day);
                String dateStr = reservationDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                
                // 해당 날짜의 스케줄들
                List<ScheduleEntity> daySchedules = schedules.stream()
                    .filter(s -> s.getScreeningDate().equals(dateStr))
                    .toList();
                
                for (ScheduleEntity schedule : daySchedules) {
                    // 각 스케줄마다 0-10개의 예약 생성
                    int reservationCount = random.nextInt(11);
                    
                    for (int i = 0; i < reservationCount; i++) {
                        MemberEntity member = members.get(random.nextInt(members.size()));
                        
                        // 예약 상태 결정 (90% 완료, 5% 취소, 5% 대기)
                        String status;
                        double statusRand = random.nextDouble();
                        if (statusRand < 0.9) {
                            status = "Y"; // 예매완료
                        } else if (statusRand < 0.95) {
                            status = "D"; // 예매취소중
                        } else {
                            status = "N"; // 예매미완료
                        }
                        
                        // 좌석 선택 (해당 상영관의 좌석 중 랜덤)
                        List<SeatEntity> screenSeats = seatRepository.findByScreen(schedule.getScreen());
                        if (!screenSeats.isEmpty()) {
                            SeatEntity seat = screenSeats.get(random.nextInt(screenSeats.size()));
                            
                            // 기본 가격 (좌석 등급에 따라)
                            int basePrice = seat.getSeatGrade().getPrice();
                            
                            reservations.add(ReservationEntity.builder()
                                .id(generateReservationId(schedule.getId(), seat.getId(), i))
                                .member(member)
                                .schedule(schedule)
                                .seat(seat)
                                .seatGrade(seat.getSeatGrade())
                                .reservationTime(reservationDate.atTime(10 + random.nextInt(12), random.nextInt(60))) // reservationDate -> reservationTime
                                .basePrice(basePrice)
                                .finalPrice(basePrice)
                                .status(status)
                                .ticketIssuanceStatus("N")
                                .build());
                        }
                    }
                }
            }
            
            reservationRepository.saveAll(reservations);
            log.info("🎫 예약 데이터 생성 완료: {}건", reservations.size());
        }
    }

    private void initializeReviews() {
        if (reviewRepository.count() == 0) {
            List<MemberEntity> members = memberRepository.findAll();
            List<MovieEntity> movies = movieRepository.findAll();
            
            if (members.isEmpty() || movies.isEmpty()) {
                log.warn("⚠️ 회원 또는 영화 데이터가 없어 리뷰 생성을 건너뜁니다.");
                return;
            }
            
            List<ReviewEntity> reviews = new ArrayList<>();
            
            String[] reviewTexts = {
                "정말 재미있게 봤습니다! 강력 추천해요.",
                "기대했던 것보다 아쉬웠네요. 그래도 볼만했어요.",
                "스토리가 탄탄하고 연기도 훌륭했습니다.",
                "액션 시퀀스가 정말 대박이었어요!",
                "가족과 함께 보기 좋은 영화입니다.",
                "예상을 뛰어넘는 반전이 있었어요.",
                "영상미가 정말 아름다웠습니다.",
                "조금 길긴 했지만 몰입도가 높았어요.",
                "배우들의 연기가 인상적이었습니다.",
                "다시 한 번 보고 싶은 영화네요.",
                "스릴 넘치는 전개가 좋았어요.",
                "감동적인 스토리였습니다.",
                "특수효과가 정말 대단했어요!",
                "유머러스한 장면들이 재미있었습니다.",
                "음악도 좋고 연출도 훌륭했어요."
            };
            
            // 각 영화마다 5-20개의 리뷰 생성
            for (MovieEntity movie : movies) {
                int reviewCount = 5 + random.nextInt(16); // 5-20개
                
                for (int i = 0; i < reviewCount; i++) {
                    MemberEntity member = members.get(random.nextInt(members.size()));
                    String content = reviewTexts[random.nextInt(reviewTexts.length)];
                    int ratingValue = 1 + random.nextInt(5); // 1-5점
                    
                    // 리뷰 작성일 (영화 개봉일 이후 랜덤)
                    LocalDate movieReleaseDate = LocalDate.parse(movie.getReleaseDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                    LocalDate reviewDate = movieReleaseDate.plusDays(random.nextInt(100));
                    
                    reviews.add(ReviewEntity.builder()
                        .member(member)
                        .movie(movie)
                        .content(content)
                        .ratingValue(ratingValue) // rating -> ratingValue
                        .createdAt(reviewDate.atTime(random.nextInt(24), random.nextInt(60)))
                        .build());
                }
            }
            
            reviewRepository.saveAll(reviews);
            log.info("⭐ 리뷰 데이터 생성 완료: {}개", reviews.size());
        }
    }

    private String generateScheduleId(String date, String screenId, Long movieId, int timeSlot) {
        // 스케줄 ID는 8자리로 제한되어 있으므로 적절히 조정
        return String.format("%s%s%d", date.substring(2), screenId, timeSlot); // YYMMDD + 상영관ID(2자리) + 타임슬롯(1자리) = 9자리
    }

    private String generateReservationId(String scheduleId, Integer seatId, int sequence) {
        // 예약 ID는 14자리로 제한되어 있으므로 적절히 조정
        return String.format("%s%04d%02d", scheduleId.substring(0, Math.min(8, scheduleId.length())), seatId, sequence);
    }
} 