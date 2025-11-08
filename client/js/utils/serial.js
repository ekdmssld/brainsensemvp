// utils/serial.js - Web Serial API 유틸리티
export class SerialPortManager {
    constructor() {
        this.port = null;
        this.reader = null;
        this.writer = null;
        this.isConnected = false;
    }

    /**
     * 브라우저가 Web Serial API를 지원하는지 확인
     */
    static isSupported() {
        return 'serial' in navigator;
    }

    /**
     * 사용 가능한 포트 검색
     */
    async requestPort() {
        try {
            if (!SerialPortManager.isSupported()) {
                throw new Error('이 브라우저는 Web Serial API를 지원하지 않습니다. Chrome 또는 Edge를 사용해주세요.');
            }

            // 사용자에게 포트 선택 프롬프트 표시
            this.port = await navigator.serial.requestPort();

            return {
                success: true,
                port: this.port
            };
        } catch (error) {
            console.error('Port request error:', error);

            if (error.name === 'NotFoundError') {
                return {
                    success: false,
                    message: '포트를 선택하지 않았습니다'
                };
            }

            return {
                success: false,
                message: error.message || '포트 검색 중 오류가 발생했습니다'
            };
        }
    }

    /**
     * 선택한 포트에 연결
     */
    async connect(baudRate = 9600) {
        try {
            if (!this.port) {
                throw new Error('포트가 선택되지 않았습니다');
            }

            await this.port.open({ baudRate });
            this.isConnected = true;

            return {
                success: true,
                message: '포트에 연결되었습니다'
            };
        } catch (error) {
            console.error('Connect error:', error);
            return {
                success: false,
                message: error.message || '포트 연결 중 오류가 발생했습니다'
            };
        }
    }

    /**
     * 포트 연결 해제
     */
    async disconnect() {
        try {
            if (this.reader) {
                await this.reader.cancel();
                this.reader = null;
            }

            if (this.port && this.isConnected) {
                await this.port.close();
                this.isConnected = false;
            }

            return {
                success: true,
                message: '포트 연결이 해제되었습니다'
            };
        } catch (error) {
            console.error('Disconnect error:', error);
            return {
                success: false,
                message: error.message || '포트 연결 해제 중 오류가 발생했습니다'
            };
        }
    }

    /**
     * 시리얼 데이터 읽기
     */
    async startReading(callback) {
        if (!this.port || !this.isConnected) {
            throw new Error('포트가 연결되지 않았습니다');
        }

        try {
            const textDecoder = new TextDecoderStream();
            const readableStreamClosed = this.port.readable.pipeTo(textDecoder.writable);
            this.reader = textDecoder.readable.getReader();

            while (true) {
                const { value, done } = await this.reader.read();
                if (done) {
                    break;
                }
                if (value) {
                    callback(value);
                }
            }
        } catch (error) {
            console.error('Read error:', error);
        }
    }

    /**
     * 시리얼 데이터 쓰기
     */
    async write(data) {
        if (!this.port || !this.isConnected) {
            throw new Error('포트가 연결되지 않았습니다');
        }

        try {
            const textEncoder = new TextEncoderStream();
            const writableStreamClosed = textEncoder.readable.pipeTo(this.port.writable);
            this.writer = textEncoder.writable.getWriter();

            await this.writer.write(data);
            await this.writer.close();
        } catch (error) {
            console.error('Write error:', error);
            throw error;
        }
    }

    /**
     * 포트 정보 가져오기
     */
    getPortInfo() {
        if (!this.port) return null;

        const info = this.port.getInfo();
        return {
            usbVendorId: info.usbVendorId,
            usbProductId: info.usbProductId,
            isConnected: this.isConnected
        };
    }
}