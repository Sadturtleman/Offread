// 페이지에서 번역할 텍스트 노드를 모으고, 앱이 돌려준 번역문을 그 자리에 채운다(#58).
//
// 노드 참조를 배열에 들고 있다가 인덱스로 되찾는다. textContent 를 통째로 바꾸면 자식 태그가
// 날아가므로, 텍스트 노드의 nodeValue 만 바꿔 링크와 강조는 살려 둔다.
(function () {
  var SKIP = ['SCRIPT', 'STYLE', 'NOSCRIPT', 'CODE', 'PRE', 'TEXTAREA', 'INPUT', 'SELECT', 'RT', 'RP'];
  // 히라가나 / 가타카나 / 한자. 일본어가 한 글자도 없으면 번역할 것이 없다.
  var JAPANESE = /[぀-ゟ゠-ヿ一-鿿]/;

  if (window.__offread) {
    window.__offread.collect();
    return;
  }

  var nodes = [];

  function translatable(node) {
    if (!node.nodeValue || !node.nodeValue.trim()) return NodeFilter.FILTER_REJECT;
    if (!JAPANESE.test(node.nodeValue)) return NodeFilter.FILTER_REJECT;
    var parent = node.parentElement;
    while (parent) {
      if (SKIP.indexOf(parent.tagName) >= 0) return NodeFilter.FILTER_REJECT;
      if (parent.getAttribute && parent.getAttribute('translate') === 'no') return NodeFilter.FILTER_REJECT;
      parent = parent.parentElement;
    }
    return NodeFilter.FILTER_ACCEPT;
  }

  function collect() {
    // 후리가나는 지운다. 남겨 두면 "漢字かんじ" 처럼 읽기가 본문에 섞여 번역이 망가진다.
    var ruby = document.querySelectorAll('rt, rp');
    for (var i = 0; i < ruby.length; i++) ruby[i].remove();

    nodes = [];
    var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, { acceptNode: translatable });
    while (walker.nextNode()) nodes.push(walker.currentNode);

    var payload = [];
    for (var j = 0; j < nodes.length; j++) {
      payload.push({ id: String(j), text: nodes[j].nodeValue.trim() });
    }
    OffreadBridge.onTextsCollected(JSON.stringify(payload));
  }

  window.__offread = {
    collect: collect,
    apply: function (id, text) {
      var node = nodes[Number(id)];
      if (node) node.nodeValue = text;
    }
  };

  collect();
})();
