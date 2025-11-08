// client/js/router.js (발췌)
import initStep3HardwareDesign from './pages/step3-hardware-design.js';

// … 라우팅 매칭 로직 내 …
if (location.pathname.endsWith('step3-hardware-design.html')) {
    initStep3HardwareDesign();
}