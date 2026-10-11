import { InboxIcon } from './icons';

export default function EmptyState({
  title = 'Aucune donnée',
  message = "Aucune donnée ne correspond aux filtres sélectionnés. Si vous attendiez des résultats, vérifiez le backend (le service peut renvoyer une réponse vide lorsqu'une erreur est ignorée côté serveur).",
}) {
  return (
    <div className="state-box state-empty">
      <div className="state-icon">
        <InboxIcon size={24} />
      </div>
      <p className="state-title">{title}</p>
      <p className="state-message">{message}</p>
    </div>
  );
}
