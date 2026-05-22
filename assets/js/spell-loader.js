document.addEventListener('DOMContentLoaded', () => {
  const target = document.getElementById('spell-markdown');
  const status = document.getElementById('spell-loader-state');
  if (!target) {
    return;
  }

  const markdownUrl = window.RELICBOUND_SPELL_MARKDOWN_URL || 'spell-compendium.md';

  const setStatus = (message, isError = false) => {
    if (!status) {
      return;
    }
    status.textContent = message;
    status.dataset.state = isError ? 'error' : 'ready';
  };

  const renderMarkdown = async () => {
    try {
      setStatus('Loading spell markdown...');
      const response = await fetch(markdownUrl, { cache: 'no-store' });
      if (!response.ok) {
        throw new Error(`Failed to load spell list: ${response.status}`);
      }

      const markdown = await response.text();
      if (!window.marked || typeof window.marked.parse !== 'function') {
        throw new Error('Markdown renderer is not available.');
      }

      target.innerHTML = window.marked.parse(markdown);
      setStatus('Loaded spell markdown.');
    } catch (error) {
      const fallback = `## Unable to load the spell list\n\nThe markdown loader could not fetch the current spell compendium.\n\nRequested source: ${markdownUrl}`;
      target.innerHTML = window.marked ? window.marked.parse(fallback) : '<p>Unable to load the spell list.</p>';
      setStatus(`Failed to load spell markdown: ${error.message}`, true);
      console.error(error);
    }
  };

  renderMarkdown();
});
