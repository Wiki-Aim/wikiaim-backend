CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER', -- USER, CONTRIBUTOR, MODERATOR, ADMIN
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name VARCHAR(100) NOT NULL,
                            slug VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT
);

CREATE TABLE pages (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
                       author_id UUID REFERENCES users(id) ON DELETE SET NULL,
                       title VARCHAR(255) NOT NULL,
                       slug VARCHAR(255) NOT NULL UNIQUE,
                       current_content JSONB, -- Le contenu validé
                       is_published BOOLEAN DEFAULT false,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE page_revisions (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                page_id UUID REFERENCES pages(id) ON DELETE CASCADE,
                                author_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
                                proposed_title VARCHAR(255) NOT NULL,
                                proposed_content JSONB NOT NULL, -- Le nouveau contenu proposé
                                commit_message TEXT,
                                status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
                                review_comment TEXT,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                reviewed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE issues (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        author_id UUID REFERENCES users(id) ON DELETE CASCADE,
                        title VARCHAR(255) NOT NULL,
                        description TEXT NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_comments (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                issue_id UUID REFERENCES issues(id) ON DELETE CASCADE,
                                author_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                content TEXT NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);