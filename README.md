# 🐍 멋쟁이 사자처럼 13기 클레오빡돌아 구석구석 Backend

## 서비스 소개
해커톤 상권분석 보고서 서비스의 백엔드 저장소입니다. 빠른 구현과 안정적 동작을 우선합니다.
<img width="723" height="408" alt="image" src="https://github.com/user-attachments/assets/e1b4e617-2361-4688-a04b-467ab1743b29" />

<img width="1921" height="1080" alt="image" src="https://github.com/user-attachments/assets/ab7f0f30-795b-48d9-8f32-56d7309e90c0" />

<img width="1921" height="1080" alt="image" src="https://github.com/user-attachments/assets/079771d7-fe71-4121-8d62-a5e546169bf7" />

![제목 없는 비디오 - Clipchamp로 제작 (2)](https://github.com/user-attachments/assets/71a07930-3bbf-45c2-aacf-6e11f8be6898)


## 🍀 지향점
1. **최적화된 성능**
   - 보고서 조회시 30초를 넘지 않도록 응답 로직을 구성했습니다.
3. **실제 인터뷰 기반 개발 진행**
   - 다양한 분야의 소상공인과 직접 인터뷰하여 개발을 진행하였습니다.
3. **효율적인 프로젝트 관리 및 협업**
   - 프론트엔드, AI 파트와 매일 만나서 실시간으로 이슈를 공유하며 개발을 진행했습니다.




## ✅ 기술스택
- **Language/Framework**: Java 17, Spring Boot 3.3.5, WebClient, Spring Data JPA
- **Web/Data**: Spring MVC, Spring Data JPA, Spring Data MongoDB  
- **Client**: WebClient (외부 AI/공공API 연동), springdoc-openapi  
- **DB**: MySQL 8.0, MongoDB 7  
- **Infra**: Docker, Docker Compose, Nginx, GitHub Actions, AWS


## 🧱 아키텍처
<img width="799" height="724" alt="image" src="https://github.com/user-attachments/assets/3b2d7e2b-cb01-4a7b-82e3-c3d0df553ff2" />



## ✅ 패키지 구조 설계
```bash
cleopatra
├─domain
│  ├─aiDescription
│  │  └─dto
│  ├─collect
│  │  ├─controller
│  │  ├─document
│  │  ├─dto
│  │  │  ├─requeset
│  │  │  └─response
│  │  ├─exception
│  │  ├─repository
│  │  ├─service
│  │  └─util
│  ├─crwal
│  │  ├─controller
│  │  ├─document
│  │  ├─dto
│  │  │  ├─blog
│  │  │  └─place
│  │  ├─exception
│  │  │  └─failure
│  │  ├─impl
│  │  ├─selector
│  │  └─service
│  ├─incomeConsumption
│  │  ├─document
│  │  ├─dto
│  │  │  ├─consumption
│  │  │  ├─description
│  │  │  └─income
│  │  ├─repository
│  │  └─service
│  ├─keywordData
│  │  ├─controller
│  │  ├─document
│  │  ├─dto
│  │  │  ├─report
│  │  │  └─webClient
│  │  ├─repository
│  │  └─service
│  ├─member
│  │  ├─controller
│  │  ├─entity
│  │  ├─repository
│  │  └─service
│  ├─openApi
│  │  ├─exception
│  │  ├─naver
│  │  │  ├─dto
│  │  │  │  ├─blog
│  │  │  │  ├─cafe
│  │  │  │  └─place
│  │  │  └─service
│  │  └─rtms
│  │      └─service
│  ├─population
│  │  ├─document
│  │  ├─dto
│  │  │  ├─age
│  │  │  ├─description
│  │  │  └─gender
│  │  ├─repository
│  │  └─service
│  └─report
│      ├─controller
│      ├─dto
│      │  ├─keyword
│      │  ├─price
│      │  └─report
│      ├─entity
│      ├─repository
│      └─service
└─global
   ├─aop
   ├─common
   │  └─enums
   │      ├─address
   │      └─keyword
   ├─config
   │  ├─crawler
   │  ├─mongoConfig
   │  └─swagger
   ├─dto
   ├─exception
   │  ├─code
   │  ├─handler
   │  └─response
   └─geo
```

## ✅ ERD
<img width="203" height="614" alt="cleopatra_erd" src="https://github.com/user-attachments/assets/941a4bbe-f53c-4143-8130-0d5245daae7a" />


# 향후 계획
1. 확장성 대비
   - 단일 모듈 내부에 헥사고날 아키텍처 적용으로 도메인/어댑터 경계 강화.
   - 스프링 배치와 큐로 데이터 수집 자동화 및 재시도·백오프 표준화.
   - 테스트 코드 보강 및 CI/CD 캐시 최적화로 빌드 시간 단축.
   - 운영 안정화를 위해 AWS ELB 도입 및 운영/스테이징 분리.
2. 로그인
   - OAuth2 기반 간편 로그인 도입 검토(토큰 로테이션 및 보안 헤더 기본화).
3. 결제
   - 보고서 단건/패키지 결제.
   - 서버 검증 + 웹훅으로 이중 확인.
   - 구현 시간 최소화, 테스트 모드 우선.
4. 서비스 분리 검증
   - 테스트 환경에서 컨테이너를 독립 배포 단위로 분리하여 확장성과 장애 격리를 검증.
   - 경계가 안정화되면 점진적 서비스 분할을 검토.
