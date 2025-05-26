Create database car_blog;


USE car_blog;
GO

-- 1. Bảng Users
CREATE TABLE dbo.Users (
    user_id      INT            IDENTITY(1,1) PRIMARY KEY,
    username     NVARCHAR(50)   NOT NULL UNIQUE,
    password     NVARCHAR(100)  NOT NULL,
    full_name    NVARCHAR(100)  NULL,
    role         NVARCHAR(20)   NOT NULL,
	email NVARCHAR(100) NOT NULL CONSTRAINT UQ_Users_Email UNIQUE,
	phone_number NVARCHAR(20) NULL,
    gender NVARCHAR(10) NULL CHECK (gender IN ('Nam', 'Nữ', 'Khác')),
    avatar NVARCHAR(500) NULL,
    birth_date DATE NULL,
    bio NVARCHAR(MAX) NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- 2. Bảng Categories
CREATE TABLE dbo.Categories (
    category_id  INT            IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(100)  NOT NULL UNIQUE
);

INSERT INTO dbo.Categories (name)
VALUES
  ('Reviews'),
  ('News'),
  ('Maintenance'),
  ('Modifications'),
  ('Classic Cars');

SELECT p.post_id, p.title, p.created_at, p.status,
       c.category_id, c.name AS category_name
FROM posts p
LEFT JOIN categories c ON p.category_id = c.category_id;




-- 3. Bảng Tags
CREATE TABLE dbo.Tags (
    tag_id       INT            IDENTITY(1,1) PRIMARY KEY,
    name         NVARCHAR(50)   NOT NULL UNIQUE
);


-- 4. Bảng Posts
CREATE TABLE dbo.Posts (
    post_id      INT            IDENTITY(1,1) PRIMARY KEY,
    title        NVARCHAR(200)  NOT NULL,
    content      NVARCHAR(MAX)  NOT NULL,
    created_at   DATETIME2      NOT NULL DEFAULT GETDATE(),
	status NVARCHAR(20) NOT NULL,
	main_image NVARCHAR(500) NUL,
	excerpt NVARCHAR(MAX) NULL,

    -- author: xóa user → xóa bài
    user_id      INT            NOT NULL,
    CONSTRAINT FK_Posts_Users FOREIGN KEY(user_id)
        REFERENCES dbo.Users(user_id)
        ON DELETE CASCADE,

    -- category có thể null, xóa category → set null
    category_id  INT            NULL,
	view_count INT NOT NULL CONSTRAINT DF_Posts_ViewCount DEFAULT 0,
    like_count INT NOT NULL CONSTRAINT DF_Posts_LikeCount DEFAULT 0;
    CONSTRAINT FK_Posts_Categories FOREIGN KEY(category_id)
        REFERENCES dbo.Categories(category_id)
        ON DELETE SET NULL
);



ALTER TABLE dbo.Posts
ADD CONSTRAINT CK_Posts_Status
    CHECK (status IN ('published','draft'));

-- 2. Tạo bảng phụ lưu nhiều ảnh (ảnh phụ) cho mỗi post
CREATE TABLE dbo.Post_Images (
    id           INT            IDENTITY(1,1) PRIMARY KEY,
    post_id      INT            NOT NULL,
    image_url    NVARCHAR(500)  NOT NULL,
    CONSTRAINT FK_PostImages_Posts FOREIGN KEY(post_id)
        REFERENCES dbo.Posts(post_id)
        ON DELETE CASCADE
);

-- 5. Bảng Post_Tags (quan hệ nhiều–nhiều)
CREATE TABLE dbo.Post_Tags (
    post_id      INT NOT NULL,
    tag_id       INT NOT NULL,
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT FK_PostTags_Posts FOREIGN KEY(post_id)
        REFERENCES dbo.Posts(post_id)
        ON DELETE CASCADE,
    CONSTRAINT FK_PostTags_Tags FOREIGN KEY(tag_id)
        REFERENCES dbo.Tags(tag_id)
        ON DELETE CASCADE
);




-- 6. Bảng Comments
CREATE TABLE dbo.Comments (
    comment_id   INT            IDENTITY(1,1) PRIMARY KEY,
    content      NVARCHAR(MAX)  NOT NULL,
    created_at   DATETIME2      NOT NULL DEFAULT GETDATE(),

    -- nếu xóa user → set null
    user_id      INT            NULL,
    CONSTRAINT FK_Comments_Users FOREIGN KEY(user_id)
        REFERENCES dbo.Users(user_id)
        ON DELETE SET NULL,

    -- KHÔNG dùng CASCADE để tránh multiple cascade paths
    post_id      INT            NOT NULL,
	parent_comment_id INT NULL,
	likes INT NOT NULL CONSTRAINT DF_Comments_Likes DEFAULT 0,
    CONSTRAINT FK_Comments_Posts FOREIGN KEY(post_id)
        REFERENCES dbo.Posts(post_id)
        ON DELETE NO ACTION
);
ALTER TABLE Comments
  DROP CONSTRAINT FK_Comments_ParentComment;
GO








-- Indexes (tùy chọn)
CREATE INDEX IDX_Posts_CreatedAt    ON dbo.Posts(created_at);
CREATE INDEX IDX_Comments_CreatedAt ON dbo.Comments(created_at);
GO

UPDATE Posts
SET main_image = 'post/' + REPLACE(main_image, '/uploads/', '')
WHERE main_image LIKE '/uploads/%';