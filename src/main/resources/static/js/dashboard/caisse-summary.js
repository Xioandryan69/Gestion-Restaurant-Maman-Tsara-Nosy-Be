// Fetch caisse summary for selected year and update DOM
async function updateCaisseSummary(containerId, year) {
  try {
    const res = await fetch(`/api/stats/caisse/year/${year}`);
    if(!res.ok) return;
    const s = await res.json();
    const container = document.getElementById(containerId);
    if(!container) return;
    container.querySelector('.caisse-entrees').textContent = Number(s.totalEntrees || 0).toLocaleString('fr-FR',{minimumFractionDigits:2}) + ' Ar';
    container.querySelector('.caisse-sorties').textContent = Number(s.totalSorties || 0).toLocaleString('fr-FR',{minimumFractionDigits:2}) + ' Ar';
    // compute benefice approximated as ventes - (achatsIng+achatsMat+maintenance)
    const ventes = Number(s.ventes || 0);
    const achats = Number(s.achatsIngredients || 0) + Number(s.achatsMaterielles || 0);
    const maintenance = Number(s.maintenance || 0);
    const benef = Math.round((ventes - (achats + maintenance)) * 100)/100;
    container.querySelector('.caisse-benefice').textContent = benef.toLocaleString('fr-FR',{minimumFractionDigits:2}) + ' Ar';
  } catch (e) {
    // ignore
  }
}

document.addEventListener('DOMContentLoaded', function(){
  const yearInput = document.getElementById('caisse-year');
  const btn = document.getElementById('caisse-refresh');
  const year = yearInput ? yearInput.value : new Date().getFullYear();
  updateCaisseSummary('caisse-summary', year);
  if(btn) btn.addEventListener('click', ()=>{
    const y = yearInput ? yearInput.value : new Date().getFullYear();
    updateCaisseSummary('caisse-summary', y);
    if(typeof renderCaisseYearPie === 'function') renderCaisseYearPie('caisseYearPie', y);
  });
});
