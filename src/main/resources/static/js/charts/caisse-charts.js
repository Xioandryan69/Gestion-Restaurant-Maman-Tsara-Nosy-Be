async function renderCaisseYearPie(canvasId, year) {
  const res = await fetch(`/api/stats/caisse/year/${year}`);
  const s = await res.json();
  const labels = ['Ventes','Achats Ing','Achats Mat','Maintenance','Autres'];
  const ventes = Number(s.ventes || 0);
  const achatsIng = Number(s.achatsIngredients || 0);
  const achatsMat = Number(s.achatsMaterielles || 0);
  const maintenance = Number(s.maintenance || 0);
  const autres = Math.max(0, Number(s.totalEntrees || 0) - ventes) + Number(s.totalSorties || 0) - (achatsIng + achatsMat + maintenance);
  const data = [ventes, achatsIng, achatsMat, maintenance, Math.max(0, autres)];

  const ctx = document.getElementById(canvasId).getContext('2d');
  new Chart(ctx, {
    type: 'pie',
    data: { labels, datasets: [{ data, backgroundColor: ['#4caf50','#2196f3','#9c27b0','#ff9800','#9e9e9e'] }] },
    options: { responsive: true }
  });
}
