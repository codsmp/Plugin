document.addEventListener('DOMContentLoaded', () => {
  const target = document.getElementById('spell-markdown');
  const status = document.getElementById('spell-loader-state');
  const outline = document.getElementById('spell-outline');
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

  const slugify = (value) => value
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');

  const buildOutline = () => {
    if (!outline) {
      return;
    }

    const headings = Array.from(target.querySelectorAll('h2'));
    if (!headings.length) {
      outline.innerHTML = '<p class="spell-outline-empty">No section headings were found.</p>';
      return;
    }

    const items = headings.map((heading) => {
      if (!heading.id) {
        heading.id = slugify(heading.textContent || 'section');
      }
      return `<a class="spell-outline-link" href="#${heading.id}">${heading.textContent}</a>`;
    });

    outline.innerHTML = items.map((item) => `<div class="spell-outline-item">${item}</div>`).join('');
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
      buildOutline();
      setStatus(`Loaded spell markdown and organized ${target.querySelectorAll('h2').length} sections.`);
    } catch (error) {
      const fallback = `## Unable to load the spell list\n\nThe markdown loader could not fetch the current spell compendium.\n\nRequested source: ${markdownUrl}`;
      target.innerHTML = window.marked ? window.marked.parse(fallback) : '<p>Unable to load the spell list.</p>';
      buildOutline();
      setStatus(`Failed to load spell markdown: ${error.message}`, true);
      console.error(error);
    }
  };

  renderMarkdown();
});
