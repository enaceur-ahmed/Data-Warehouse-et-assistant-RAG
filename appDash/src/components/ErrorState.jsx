import { AlertIcon, RefreshIcon } from './icons';

export default function ErrorState({ error, onRetry }) {
  const message =
    error && error.message
      ? error.message
      : 'Une erreur inattendue est survenue lors du chargement des données.';

  return (
    <div className="state-box state-error" role="alert">
      <div className="state-icon">
        <AlertIcon size={24} />
      </div>
      <p className="state-title">Erreur de chargement</p>
      <p className="state-message">{message}</p>
      {onRetry && (
        <button type="button" className="btn btn-primary" onClick={onRetry}>
          <RefreshIcon size={15} />
          Réessayer
        </button>
      )}
    </div>
  );
}
