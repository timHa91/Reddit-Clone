# RedditClone

RedditClone is a RESTful web application built with Spring Boot that replicates the functionality of the popular website Reddit. It allows users to create subreddits, make posts, leave comments, and vote on posts and comments. The application also features user authentication using JSON Web Tokens (JWT) and includes an email service for user activation and comment notifications.

## Features

- **User Authentication**: RedditClone uses JWT for user authentication, providing a secure way for users to register, log in, and access their accounts.

- **Subreddits**: Users can create and join subreddits, allowing them to categorize and follow discussions based on their interests.

- **Posts**: Users can create posts within subreddits, including text posts and links. They can also upvote or downvote posts.

- **Comments**: Users can leave comments on posts, fostering discussions within the community.

- **Voting**: RedditClone supports both post and comment voting, enabling users to express their opinions on content.

- **Email Service**: The application includes an email service for user activation and notifications when new comments are posted on their content.

## Prerequisites

Before you begin, ensure you have met the following requirements:

- Java Development Kit (JDK)
- Maven
- Spring Boot
- Your preferred Integrated Development Environment (IDE)
- SMTP server configuration for email notifications

## Getting Started

To get started with RedditClone, follow these steps:

1. Clone the repository: `git clone https://github.com/your-username/RedditClone.git`

2. Configure the application properties, including your database settings and email service configurations.

3. Build the project using Maven: `mvn clean install`

4. Run the application: `mvn spring-boot:run`

5. Access the application at `http://localhost:8080`.

## Usage

1. Register a new account or log in if you already have one.

2. Create or join subreddits to start participating in discussions.

3. Create posts within subreddits or comment on existing posts.

4. Upvote or downvote posts and comments to express your opinion.

5. Activate email notifications for new comments in your account settings.
