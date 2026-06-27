package com.altf4.tetriswebview_kt

object TetrisScripts {

    private const val CSS_STYLES = """
        * { background: black !important; }
        .tetris-container {
            position: fixed !important;
            top: 0 !important;
            left: 0 !important;
            width: 100vw !important;
            height: 100vh !important;
        }
        #tetris-game-iframe { width: 100%; height: 100%; border: none; }
        footer, nav, div:has(> div > video) { display: none !important; }
    """

    val INJECT_CSS = """
        (function() {
            var s = document.createElement('style');
            s.textContent = '${CSS_STYLES.replace("\n", " ")}';
            document.head.appendChild(s);
        })();
    """.trimIndent()

    val TOGGLE_PAUSE = """
        (function() {
          try {
             var frame = document.querySelector('iframe');
             if (frame && frame.contentDocument) {
                var canvas = frame.contentDocument.querySelector('canvas');
                if (canvas) {
                   canvas.focus();
                   var downEvent = new KeyboardEvent('keydown', { key: 'Escape', keyCode: 27, bubbles: true });
                   var upEvent = new KeyboardEvent('keyup', { key: 'Escape', keyCode: 27, bubbles: true });
                   canvas.dispatchEvent(downEvent);
                   canvas.dispatchEvent(upEvent);
                }
             }
          } catch(e) { console.log(e); }
        })();
    """.trimIndent()

    val CHECK_IS_PAUSED = """
        (function() {
            var frame = document.querySelector('iframe');
            if (frame && frame.contentWindow && frame.contentWindow.cc) {
                return String(frame.contentWindow.cc.game.isPaused());
            }
            return 'false';
        })();
    """.trimIndent()
}
