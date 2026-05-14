-- ============================================================
-- Cart persistence — 2026-05-07
-- Table: cart
-- Purpose: persist JSF session cart across sessions (badge + restore)
-- ============================================================

CREATE TABLE IF NOT EXISTS cart (
    idCart        INT          NOT NULL AUTO_INCREMENT,
    cartPlayerId  INT          NOT NULL,
    cartClubId    INT          NOT NULL,
    cartType      ENUM('COTISATION','GREENFEE','LESSON','SUBSCRIPTION') NOT NULL,
    cartItemsJson JSON         NOT NULL,
    cartTotal     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cartStatus    ENUM('PENDING','COMPLETED','CANCELED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    cartCreatedAt DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cartModificationDate DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (idCart),
    UNIQUE KEY ukCartPlayerClubType (cartPlayerId, cartClubId, cartType),
    CONSTRAINT fkCartPlayer FOREIGN KEY (cartPlayerId) REFERENCES player  (idplayer),
    CONSTRAINT fkCartClub   FOREIGN KEY (cartClubId)   REFERENCES club    (idclub)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
