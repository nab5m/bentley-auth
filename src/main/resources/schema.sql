CREATE TABLE `user`
(
    id            BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    firstName     VARCHAR(255),
    lastName      VARCHAR(255),
    status        VARCHAR(40)  NOT NULL,
    createdAt     DATETIME     NOT NULL,
    updatedAt     DATETIME     NOT NULL,
    deactivatedAt DATETIME,
    UNIQUE (email),
    UNIQUE (phone)
);

CREATE TABLE `userVerification`
(
    id         BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    userId     BIGINT       NOT NULL,
    code       VARCHAR(255) NOT NULL,
    verifiedAt DATETIME,
    expiresAt  DATETIME     NOT NULL,
    createdAt  DATETIME     NOT NULL,
    updatedAt  DATETIME     NOT NULL,
    FOREIGN KEY (userId) REFERENCES `user` (id)
);

CREATE TABLE `refreshToken`
(
    id        BIGINT   NOT NULL PRIMARY KEY AUTO_INCREMENT,
    userId    BIGINT   NOT NULL,
    token     TEXT     NOT NULL,
    expiresAt DATETIME NOT NULL,
    createdAt DATETIME NOT NULL,
    updatedAt DATETIME NOT NULL,
    FOREIGN KEY (userId) REFERENCES `user` (id)
);

CREATE TABLE `socialUser`
(
    id             BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    userId         BIGINT       NOT NULL,
    registrationId VARCHAR(255) NOT NULL,
    subject        VARCHAR(255) NOT NULL,
    createdAt      DATETIME     NOT NULL,
    updatedAt      DATETIME     NOT NULL,
    UNIQUE (registrationId, subject),
    FOREIGN KEY (userId) REFERENCES `user` (id)
);