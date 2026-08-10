async function renderIngredientsYearChart(canvasId, year, topN = 10) {
  const res = await fetch(`/api/stats/ingredients/year/${year}`);
  const data = await res.json();
  // data is a list of {ingredientId, nom, entreeTotal, sortieTotal, stockCurrent}
  const items = data.map(i => ({
    nom: i.nom || (`#${i.ingredientId}`),
    entree: Number(i.entreeTotal || 0),
    sortie: Number(i.sortieTotal || 0),
    stock: Number(i.stockCurrent || 0)
  }));
  // sort by stock desc
  items.sort((a,b) => b.stock - a.stock);
  const sliced = items.slice(0, topN).reverse();
  const labels = sliced.map(i => i.nom);
  const entreeData = sliced.map(i => i.entree);
  const sortieData = sliced.map(i => i.sortie);
  const stockData = sliced.map(i => i.stock);

  const ctx = document.getElementById(canvasId).getContext('2d');
  new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        { label: 'Entrées', data: entreeData, backgroundColor: 'rgba(75,192,192,0.6)' },
        { label: 'Sorties', data: sortieData, backgroundColor: 'rgba(255,99,132,0.6)' },
        { label: 'Stock', data: stockData, backgroundColor: 'rgba(255,205,86,0.6)' }
      ]
    },
    options: { responsive: true, scales: { x: { stacked: true }, y: { stacked: false } } }
  });
}
