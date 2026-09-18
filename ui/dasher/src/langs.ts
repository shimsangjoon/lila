import { type VNode } from 'snabbdom';

import { hl, bindNonPassive } from 'lib/view';

import { licon } from 'lib/licon';

import { PaneCtrl } from './interfaces';
import { header } from './util';

type Code = string;
type Name = string;

export type Lang = [Code, Name];

export interface LangsData {
  current: Code;
  accepted: Code[];
  list: Lang[];
}

export class LangsCtrl extends PaneCtrl {
  private searchInput: string = '';

  render = (): VNode =>
    hl('div.sub.langs', [
      header(i18n.site.language, this.close),
      hl(
        'form',
        {
          attrs: { method: 'post', action: '/translation/select' },
          on: { submit: e => e.preventDefault() },
        },
        [
          hl(
            'input',
            {
              attrs: { type: 'search', name: 'search', placeholder: i18n.site.searchLanguage },
              hook: bindNonPassive('input', (e: Event) => {
                e.preventDefault();
                const val = (e.target as HTMLInputElement).value;
                this.searchInput = val;
                this.redraw();
              }, this.redraw),
            },
            '',
          ),
          this.filteredList().map(([code, name]: Lang) =>
            hl(
              'button',
              {
                class: {
                  current: this.isCurrent(code),
                  accepted: this.isAccepted(code),
                },
                attrs: { type: 'submit', name: 'lang', value: code, title: code },
              },
              name,
            ),
          ),
        ],
      ),
      hl(
        'a.help.text',
        { attrs: { href: 'https://crowdin.com/project/lichess', 'data-icon': licon.Heart } },
        'Help translate Lichess',
      ),
    ]);

  private get data() {
    return this.root.data.lang;
  }

  private readonly isCurrent = (code: Code) => this.data.current === code;
  private readonly isAccepted = (code: Code) => this.data.accepted.includes(code);

  private readonly list = () => [
    ...this.data.list.filter(([code, _]) => this.isCurrent(code) || this.isAccepted(code)),
    ...this.data.list,
  ];

  private readonly filteredList = (): Lang[] => {
    const all = this.list();
    if (!this.searchInput) return all;
    const query = this.searchInput.toLowerCase();
    return all.filter(([, name]) => name.toLowerCase().includes(query));
  };
}
