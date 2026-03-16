-- Create feedback table for citizen feedback on completed recycling requests
CREATE TABLE IF NOT EXISTS feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recycle_request_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_feedback_recycle_request 
        FOREIGN KEY (recycle_request_id) 
        REFERENCES recycle_request(id) ON DELETE CASCADE,
    
    CONSTRAINT unique_feedback_per_request 
        UNIQUE (recycle_request_id)
);

-- Create indexes for common queries
CREATE INDEX idx_feedback_user_id ON feedback(user_id);
CREATE INDEX idx_feedback_recycle_request_id ON feedback(recycle_request_id);
CREATE INDEX idx_feedback_created_at ON feedback(created_at DESC);
CREATE INDEX idx_feedback_category ON feedback(category);
