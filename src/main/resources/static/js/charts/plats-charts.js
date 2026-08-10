async function renderPlatCostChart(canvasId, platId) {
  const res = await fetch(`/api/stats/plats/${platId}/cost`);
  const d = await res.json();
  const prixAchat = Number(d.prixAchat || 0);
  const profit = Number(d.profitPerUnit || 0);
  const prixVente = prixAchat + profit;

  const ctx = document.getElementById(canvasId).getContext('2d');
  new Chart(ctx, {
    type: 'bar',
    data: { labels: ['Coût achat','Prix vente','Profit'], datasets: [{ data: [prixAchat, prixVente, profit], backgroundColor: ['#2196f3','#4caf50','#ff9800'] }] },
    options: { responsive: true }
  });
}
