# 🤖 Anonymous Feedback Bot

Простий Telegram-бот для збору **анонімних відгуків** співробітників автосервісу.  
Бот аналізує повідомлення через **Gemini API (Google AI)**, визначає настрій, рівень критичності, рекомендації та записує результати у **PostgreSQL** і **Google Sheets**.


<br><br>

## 🧩 Основний функціонал

- Вибір **ролі** (механік / електрик / менеджер) та **філії** при першому запуску.
- Надсилання будь-яких повідомлень (скарги, побажання, пропозиції).
- Аналіз повідомлень через **Google AI (Gemini)**:
  - Визначення настрою: позитивний / нейтральний / негативний.
  - Визначення рівня критичності (1–5).
  - Генерація поради для вирішення.
- Збереження фідбеків у базі **PostgreSQL**.
- Дублювання у **Google Sheets** для зручності перегляду.


<br><br>

## ⚙️ Технології

- **Java 17+**
- **Spring Boot**
- **PostgreSQL**
- **Hibernate / JPA**
- **Telegram Bots API**
- **Google AI (Gemini API)**
- **Google Sheets API**


<br><br>

## 🚀 Як запустити

### 1️⃣ Вимоги
- Встанови **Java 17+**, **Maven**, **PostgreSQL**.
- Створи Telegram-бота через [BotFather](https://t.me/BotFather).
- Створи Google Cloud проект та увімкни:
  - Google Sheets API  
  - Google Drive API  
  - Google Generative Language API (Gemini)

---

### 2️⃣ Створи базу даних

CREATE DATABASE feedback_bot;


<br><br>

### 3️⃣ Налаштуй `application.properties`

`src/main/resources/application.properties`:


spring.application.name=Anonymous Feedback Bot

spring.datasource.url=jdbc:postgresql://localhost:5432/feedback_bot
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

telegram.bot.username=your_bot_username
telegram.bot.token=your_bot_token

google.api.key=your_gemini_api_key

google.api.credentials.path=src/main/resources/credentials.json
google.sheets.spreadsheetId=your_spreadsheet_id


> ⚠️ Додай файл `credentials.json` (Google Service Account) у `src/main/resources/`.


<br><br>

### 4️⃣ Запуск бота

#### Через IntelliJ IDEA

Запусти клас
`AnonymousFeedbackBotApplication.java` → **Run** ▶️

#### Через термінал

bash
mvn spring-boot:run


<br><br>

### 5️⃣ Перевір роботу

1. Знайди свого бота у Telegram.
2. Натисни **Start**.
3. Обери роль та філію.
4. Відправ відгук.
5. Дані збережуться у базі та з’являться у Google Sheets.

---

## ✅ Статус

Версія без бонусних завдань (Trello інтеграції та адмін-панелі).
Функціонал повністю робочий і готовий до демонстрації.
