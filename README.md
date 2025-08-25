# 🐍 멋쟁이 사자처럼 13기 클레오빡돌아 구석구석 Backend
해커톤 상권분석 보고서 서비스의 백엔드 저장소입니다. 빠른 구현과 안정적 동작을 우선합니다.



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



## ✅ 패키기 구조 설계
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

