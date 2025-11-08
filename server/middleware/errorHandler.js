// middleware/errorHandler.js - 전역 에러 핸들러
export const errorHandler = (err, req, res, next) => {
    console.error('Error:', err);

    let error = { ...err };
    error.message = err.message;

    // Mongoose 중복 키 에러
    if (err.code === 11000) {
        const field = Object.keys(err.keyValue)[0];
        error.message = `${field}가 이미 사용 중입니다`;
        return res.status(400).json({
            success: false,
            message: error.message
        });
    }

    // Mongoose 검증 에러
    if (err.name === 'ValidationError') {
        const messages = Object.values(err.errors).map(e => e.message);
        error.message = messages.join(', ');
        return res.status(400).json({
            success: false,
            message: error.message
        });
    }

    // Mongoose CastError (잘못된 ObjectId)
    if (err.name === 'CastError') {
        error.message = '잘못된 ID 형식입니다';
        return res.status(400).json({
            success: false,
            message: error.message
        });
    }

    // 기본 에러 응답
    res.status(err.statusCode || 500).json({
        success: false,
        message: error.message || '서버 오류가 발생했습니다',
        ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
};