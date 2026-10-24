# TODO fCustomItems

- Nada foi enviado ao remoto ainda (`https://github.com/FlawlessPT/fCustomItems`): sem push feito.
- `/customitem reload` (recarregar os ficheiros sem `/reload`).
- `/customitem list` (listar ids).
- Atributos e flags de item (`ItemFlag`, modificadores de atributos).
- Cor de cabedal (`LeatherArmorMeta`) em campos legíveis; hoje usa `item:`.
- Ids numéricos de material não são suportados (só nomes Bukkit 1.8).
- `&` literal em nomes volta como cor ao recarregar.
- `ItemRegistry` sem sincronização (só thread principal).

## Migração para Paper 1.21 (24/10/2026)
- Itens custom já existentes perdem a marca (o NBT antigo não passa para o `PersistentDataContainer`, chave `fcustomitems:id`): voltar a dá-los com `/customitem give`.
- Nos ficheiros de itens do servidor, tirar o `data`/`:data` e trocar nomes de material do 1.8 que já não existem (dão o aviso de material desconhecido).
