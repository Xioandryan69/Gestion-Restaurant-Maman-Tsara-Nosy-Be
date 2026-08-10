// Fill prix achat and benefice for each plat in the list
document.addEventListener('DOMContentLoaded', function(){
  const rows = document.querySelectorAll('table.data-table tbody tr');
  let totalBenefice = 0;
  let count = 0;
  rows.forEach(async (tr) => {
    const idCell = tr.querySelector('td[data-plat-id]');
    if (!idCell) return;
    const platId = idCell.getAttribute('data-plat-id');
    // extract prix vente from cell (assumes format '12345.67 Ar' or '0,00 Ar')
    const prixVCell = tr.querySelector('.plat-prixvente');
    const prixVText = prixVCell ? prixVCell.textContent.trim().replace(' Ar','').replace(/,/g,'.') : '0';
    const prixV = Number(prixVText) || 0;

    try {
      const res = await fetch(`/api/stats/plats/${platId}/cost`);
      if (!res.ok) throw new Error('no data');
      const js = await res.json();
      const prixA = Number(js.prixAchat || 0);
      const benef = Math.round((prixV - prixA) * 100)/100;
      const prixACell = tr.querySelector('.plat-prixachat');
      const benefCell = tr.querySelector('.plat-benefice');
      if (prixACell) prixACell.textContent = prixA.toLocaleString('fr-FR', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' Ar';
      if (benefCell) benefCell.textContent = benef.toLocaleString('fr-FR', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' Ar';
      totalBenefice += benef;
      count++;
      // update aggregate
      const totalEl = document.getElementById('plats-total-benefice');
      if (totalEl) totalEl.textContent = totalBenefice.toLocaleString('fr-FR', {minimumFractionDigits:2, maximumFractionDigits:2}) + ' Ar';
    } catch (e) {
      // leave placeholders
    }
  });
});
