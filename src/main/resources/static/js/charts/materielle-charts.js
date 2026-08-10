// Requires Chart.js available at /static/cdn/chart.js
async function renderMaterielleRangeChart(canvasId, debut, fin, variable = 'achats') {
  // variable: 'achats' or 'maintenances'
  const res = await fetch(`/api/stats/materielles/range?debut=${debut}&fin=${fin}`);
  const data = await res.json();
  const sums = {};

  if (variable === 'maintenances') {
    const maints = data.maintenances || [];
    maints.forEach(m => {
      const d = m.dateMaintenance ? m.dateMaintenance : 'unknown';
      const montant = m.cout ? Number(m.cout) : 0;
      sums[d] = (sums[d] || 0) + montant;
    });
  } else {
    // default to achats
    const achats = data.achats || [];
    achats.forEach(a => {
      const d = a.dateEntree ? a.dateEntree : 'unknown';
      const montant = (a.prixAchat && a.quantite) ? (Number(a.prixAchat) * Number(a.quantite)) : 0;
      sums[d] = (sums[d] || 0) + montant;
    });
  }

  const labels = Object.keys(sums).sort();
  const values = labels.map(l => sums[l]);

  const ctx = document.getElementById(canvasId).getContext('2d');
  // destroy existing chart instance if present to avoid overlay
  if (ctx.__chartInstance) {
    ctx.__chartInstance.destroy();
  }
  ctx.__chartInstance = new Chart(ctx, {
    type: 'bar',
    data: { labels, datasets: [{ label: variable === 'maintenances' ? 'Maintenances' : 'Achats matériels', data: values, backgroundColor: 'rgba(54,162,235,0.6)' }] },
    options: { responsive: true }
  });
}
