//Paul Stothard, University of Alberta

function showMouseover(evt, message) {
  var PADDING = 8;
  var X_SHIFT = 20;
  var Y_SHIFT = 20;

  var svgDoc = evt.target.ownerDocument;

  var translateX = svgDoc.rootElement.currentTranslate.x;
  var translateY = svgDoc.rootElement.currentTranslate.y;
  var scale = 1 / svgDoc.rootElement.currentScale;

  var effectiveDocWidth = svgDoc.rootElement.getAttribute("width") - translateX;
  var effectiveDocHeight =
    svgDoc.rootElement.getAttribute("height") - translateY;

  var targetText = svgDoc.getElementById("mouseoverBox");
  var x = evt.clientX - translateX + X_SHIFT;
  var y = evt.clientY - translateY + Y_SHIFT;

  var newText = svgDoc.createTextNode(message);
  targetText.replaceChild(newText, targetText.firstChild);
  var textBounds = targetText.getBBox();

  y = y + textBounds.height;

  if (x + textBounds.width + PADDING > effectiveDocWidth) {
    x = x - (x + textBounds.width + PADDING - effectiveDocWidth);
    if (y > effectiveDocWidth / 2) {
      y = y - Y_SHIFT - Y_SHIFT - textBounds.height;
    } else {
    }
  }

  if (y + textBounds.height + PADDING > effectiveDocHeight) {
    y = y - (y + textBounds.height + PADDING - effectiveDocHeight);
  }

  if (x - PADDING < 0) {
    x = 0 + PADDING;
  }
  if (y - textBounds.height - PADDING < 0) {
    y = 0 + textBounds.height + PADDING;
  }

  targetText.setAttribute("x", x);
  targetText.setAttribute("y", y);
  textBounds = targetText.getBBox();
  targetTextBackground = svgDoc.getElementById("mouseoverBoxBackground");
  targetTextBackground.setAttribute(
    "transform",
    "scale(" + scale + "," + scale + ")"
  );
  targetTextBackground.setAttribute("x", textBounds.x - PADDING / 2);
  targetTextBackground.setAttribute("y", textBounds.y - PADDING / 2);
  targetTextBackground.setAttribute("width", textBounds.width + PADDING);
  targetTextBackground.setAttribute("height", textBounds.height + PADDING);
  targetText.setAttribute("transform", "scale(" + scale + "," + scale + ")");
}

function showMouseout(evt) {
  var svgDoc = evt.target.ownerDocument;
  var targetText = svgDoc.getElementById("mouseoverBox");
  var newText = svgDoc.createTextNode("");
  targetText.setAttribute("x", 0);
  targetText.setAttribute("y", 0);
  targetText.replaceChild(newText, targetText.firstChild);
  targetTextBackground = svgDoc.getElementById("mouseoverBoxBackground");
  targetTextBackground.setAttribute("x", 0);
  targetTextBackground.setAttribute("y", 0);
  targetTextBackground.setAttribute("width", 0);
  targetTextBackground.setAttribute("height", 0);
}
